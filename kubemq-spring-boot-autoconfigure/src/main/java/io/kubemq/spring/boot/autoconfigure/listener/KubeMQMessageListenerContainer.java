package io.kubemq.spring.boot.autoconfigure.listener;

import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.cq.CommandMessageReceived;
import io.kubemq.sdk.cq.CommandResponseMessage;
import io.kubemq.sdk.cq.CommandsSubscription;
import io.kubemq.sdk.cq.QueriesSubscription;
import io.kubemq.sdk.cq.QueryMessageReceived;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.sdk.exception.KubeMQException;
import io.kubemq.sdk.pubsub.EventMessageReceived;
import io.kubemq.sdk.pubsub.EventStoreMessageReceived;
import io.kubemq.sdk.pubsub.EventsStoreSubscription;
import io.kubemq.sdk.pubsub.EventsStoreType;
import io.kubemq.sdk.pubsub.EventsSubscription;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueueMessageReceived;
import io.kubemq.sdk.queues.QueuesPollRequest;
import io.kubemq.sdk.queues.QueuesPollResponse;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerEndpoint.KubeMQListenerType;
import io.kubemq.spring.boot.autoconfigure.support.KubeMQCoroutineBridge;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.concurrent.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.ErrorHandler;

/**
 * Manages the lifecycle of a single KubeMQ listener: subscribing to channels,
 * dispatching messages to the annotated method, and handling errors.
 *
 * <p>Implements {@link SmartLifecycle} for proper Spring lifecycle integration.
 * The SDK handles reconnection transparently — this container stays alive.
 */
@ThreadSafe
public class KubeMQMessageListenerContainer implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(KubeMQMessageListenerContainer.class);
    private static final int DEFAULT_PHASE = Integer.MAX_VALUE - 100;

    private final MethodKubeMQListenerEndpoint endpoint;
    private final PubSubClient pubSubClient;
    private final QueuesClient queuesClient;
    private final CQClient cqClient;
    private final ErrorHandler errorHandler;
    private final KubeMQMessageConverter messageConverter;
    private final KubeMQCoroutineBridge coroutineSupport; // nullable — absent when kotlin module not on classpath
    private final boolean isSuspend;
    private final Duration shutdownTimeout;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final List<Runnable> activeCancellations = new CopyOnWriteArrayList<>();
    private volatile ExecutorService queuePollExecutor;
    private volatile Semaphore concurrencySemaphore;
    private final AtomicLong currentBackoffMs;
    private long initialBackoffMs = 1000L;
    private long maxBackoffMs = 30_000L;
    private double backoffMultiplier = 2.0;

    public KubeMQMessageListenerContainer(
            MethodKubeMQListenerEndpoint endpoint,
            PubSubClient pubSubClient,
            QueuesClient queuesClient,
            CQClient cqClient,
            ErrorHandler errorHandler,
            KubeMQMessageConverter messageConverter,
            KubeMQCoroutineBridge coroutineSupport,
            Duration shutdownTimeout) {
        this.endpoint = endpoint;
        this.pubSubClient = pubSubClient;
        this.queuesClient = queuesClient;
        this.cqClient = cqClient;
        this.errorHandler = errorHandler;
        this.messageConverter = messageConverter;
        this.coroutineSupport = coroutineSupport;
        this.isSuspend = coroutineSupport != null
                && coroutineSupport.isSuspendFunction(endpoint.getMethod());
        this.shutdownTimeout = shutdownTimeout != null ? shutdownTimeout : Duration.ofSeconds(30);
        this.currentBackoffMs = new AtomicLong(initialBackoffMs);
    }

    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        log.info("Starting KubeMQ listener container '{}' [type={}, channels={}]",
                endpoint.getId(), endpoint.getType(), endpoint.getChannels());
        try {
            switch (endpoint.getType()) {
                case EVENT -> startEventSubscriptions();
                case EVENT_STORE -> startEventStoreSubscriptions();
                case QUEUE -> startQueuePolling();
                case COMMAND -> startCommandSubscription();
                case QUERY -> startQuerySubscription();
            }
        } catch (Exception e) {
            running.set(false);
            cancelAllSubscriptions();
            shutdownQueuePollExecutor();
            throw new IllegalStateException(
                    "Failed to start KubeMQ listener container '" + endpoint.getId() + "'", e);
        }
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        log.info("Stopping KubeMQ listener container '{}'", endpoint.getId());
        cancelAllSubscriptions();
        shutdownQueuePollExecutor();
        if (coroutineSupport != null) {
            coroutineSupport.cancelScope(endpoint.getId());
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public boolean isAutoStartup() {
        return endpoint.isAutoStartup();
    }

    @Override
    public int getPhase() {
        return DEFAULT_PHASE;
    }

    public String getId() {
        return endpoint.getId();
    }

    public KubeMQListenerType getType() {
        return endpoint.getType();
    }

    public String[] getChannels() {
        return endpoint.getChannels();
    }

    public String getGroup() {
        return endpoint.getGroup();
    }

    public int getConcurrency() {
        return endpoint.getConcurrency();
    }

    // ==================== Subscription Starters ====================

    private void startEventSubscriptions() {
        int concurrency = effectiveConcurrency();
        for (String channel : endpoint.getChannels()) {
            EventsSubscription subscription = EventsSubscription.builder()
                    .channel(channel)
                    .group(endpoint.getGroup())
                    .maxConcurrentCallbacks(concurrency)
                    .onReceiveEventCallback(this::handleEventMessage)
                    .onErrorCallback(this::handleSubscriptionError)
                    .build();
            pubSubClient.subscribeToEvents(subscription);
            activeCancellations.add(subscription::cancel);
            log.debug("Subscribed to events channel '{}' [group='{}', concurrency={}]",
                    channel, endpoint.getGroup(), concurrency);
        }
    }

    private void startEventStoreSubscriptions() {
        int concurrency = effectiveConcurrency();
        EventsStoreType storeType = endpoint.getEventsStoreType() != null
                ? endpoint.getEventsStoreType()
                : EventsStoreType.StartNewOnly;

        for (String channel : endpoint.getChannels()) {
            EventsStoreSubscription subscription = EventsStoreSubscription.builder()
                    .channel(channel)
                    .group(endpoint.getGroup())
                    .maxConcurrentCallbacks(concurrency)
                    .eventsStoreType(storeType)
                    .eventsStoreSequenceValue(endpoint.getEventsStoreValue())
                    .onReceiveEventCallback(this::handleEventStoreMessage)
                    .onErrorCallback(this::handleSubscriptionError)
                    .build();
            pubSubClient.subscribeToEventsStore(subscription);
            activeCancellations.add(subscription::cancel);
            log.debug("Subscribed to events-store channel '{}' [type={}, group='{}']",
                    channel, storeType, endpoint.getGroup());
        }
    }

    private void startQueuePolling() {
        int concurrency = Math.max(1, effectiveConcurrency());
        concurrencySemaphore = new Semaphore(concurrency);
        queuePollExecutor = Executors.newFixedThreadPool(
                endpoint.getChannels().length,
                r -> {
                    Thread t = new Thread(r, "kubemq-queue-poll-" + endpoint.getId());
                    t.setDaemon(true);
                    return t;
                });

        int pollTimeout = endpoint.getPollTimeoutSeconds() > 0
                ? endpoint.getPollTimeoutSeconds() : 5;
        int maxMessages = endpoint.getMaxPollMessages() > 0
                ? endpoint.getMaxPollMessages() : 1;
        int visibilitySeconds = endpoint.getVisibilityTimeoutSeconds();

        for (String channel : endpoint.getChannels()) {
            queuePollExecutor.submit(() -> pollLoop(
                    channel, pollTimeout, maxMessages, visibilitySeconds));
        }
    }

    private void startCommandSubscription() {
        int concurrency = effectiveConcurrency();
        String channel = endpoint.getChannels()[0];
        CommandsSubscription subscription = CommandsSubscription.builder()
                .channel(channel)
                .group(endpoint.getGroup())
                .maxConcurrentCallbacks(concurrency)
                .onReceiveCommandCallback(this::handleCommandMessage)
                .onErrorCallback(this::handleSubscriptionError)
                .build();
        cqClient.subscribeToCommands(subscription);
        activeCancellations.add(subscription::cancel);
        log.debug("Subscribed to commands channel '{}' [group='{}', concurrency={}]",
                channel, endpoint.getGroup(), concurrency);
    }

    private void startQuerySubscription() {
        int concurrency = effectiveConcurrency();
        String channel = endpoint.getChannels()[0];
        QueriesSubscription subscription = QueriesSubscription.builder()
                .channel(channel)
                .group(endpoint.getGroup())
                .maxConcurrentCallbacks(concurrency)
                .onReceiveQueryCallback(this::handleQueryMessage)
                .onErrorCallback(this::handleSubscriptionError)
                .build();
        cqClient.subscribeToQueries(subscription);
        activeCancellations.add(subscription::cancel);
        log.debug("Subscribed to queries channel '{}' [group='{}', concurrency={}]",
                channel, endpoint.getGroup(), concurrency);
    }

    // ==================== Message Handlers ====================

    private void handleEventMessage(EventMessageReceived message) {
        invokeListenerMethod(message);
    }

    private void handleEventStoreMessage(EventStoreMessageReceived message) {
        invokeListenerMethod(message);
    }

    private void handleCommandMessage(CommandMessageReceived message) {
        try {
            if (isSuspend) {
                coroutineSupport.invokeSuspend(endpoint.getId(), endpoint.getBean(),
                        endpoint.getMethod(), message)
                    .thenAccept(result -> {
                        CommandResponseMessage response = buildCommandResponse(message, result);
                        cqClient.sendResponseMessage(response);
                    })
                    .exceptionally(ex -> {
                        log.error("Command handler '{}' error", endpoint.getId(), ex);
                        handleError(ex);
                        cqClient.sendResponseMessage(CommandResponseMessage.builder()
                            .commandReceived(message).isExecuted(false)
                            .error("Internal server error").build());
                        return null;
                    });
                return;
            }
            Object result = invokeListenerMethodDirect(message);
            CommandResponseMessage response = buildCommandResponse(message, result);
            cqClient.sendResponseMessage(response);
        } catch (Exception e) {
            log.error("Command handler '{}' error", endpoint.getId(), e);
            handleError(e);
            CommandResponseMessage errorResponse = CommandResponseMessage.builder()
                    .commandReceived(message)
                    .isExecuted(false)
                    .error("Internal server error")
                    .build();
            cqClient.sendResponseMessage(errorResponse);
        }
    }

    private CommandResponseMessage buildCommandResponse(CommandMessageReceived command, Object result) {
        if (result instanceof CommandResponseMessage crm) {
            return crm;
        } else if (result instanceof Boolean b) {
            return CommandResponseMessage.builder()
                    .commandReceived(command)
                    .isExecuted(b)
                    .build();
        } else if (result == null) {
            // void return — treat as success
            return CommandResponseMessage.builder()
                    .commandReceived(command)
                    .isExecuted(true)
                    .build();
        } else {
            // Unexpected return type — defensive error
            return CommandResponseMessage.builder()
                    .commandReceived(command)
                    .isExecuted(false)
                    .error("Internal server error")
                    .build();
        }
    }

    private void handleQueryMessage(QueryMessageReceived message) {
        try {
            if (isSuspend) {
                coroutineSupport.invokeSuspend(endpoint.getId(), endpoint.getBean(),
                        endpoint.getMethod(), message)
                    .thenAccept(result -> {
                        if (result instanceof QueryResponseMessage qrm) {
                            cqClient.sendResponseMessage(qrm);
                        } else {
                            cqClient.sendResponseMessage(QueryResponseMessage.builder()
                                .queryReceived(message).error("Internal server error").build());
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("Query handler '{}' error", endpoint.getId(), ex);
                        handleError(ex);
                        cqClient.sendResponseMessage(QueryResponseMessage.builder()
                            .queryReceived(message).error("Internal server error").build());
                        return null;
                    });
                return;
            }
            Object result = invokeListenerMethodDirect(message);
            if (result instanceof QueryResponseMessage qrm) {
                cqClient.sendResponseMessage(qrm);
            } else {
                log.warn("Query handler '{}' returned non-QueryResponseMessage: {}",
                        endpoint.getId(), result != null ? result.getClass().getName() : "null");
                QueryResponseMessage errorResponse = QueryResponseMessage.builder()
                        .queryReceived(message)
                        .isExecuted(false)
                        .error("Internal server error")
                        .build();
                cqClient.sendResponseMessage(errorResponse);
            }
        } catch (Exception e) {
            log.error("Query handler '{}' error", endpoint.getId(), e);
            handleError(e);
            QueryResponseMessage errorResponse = QueryResponseMessage.builder()
                    .queryReceived(message)
                    .isExecuted(false)
                    .error("Internal server error")
                    .build();
            cqClient.sendResponseMessage(errorResponse);
        }
    }

    // ==================== Queue Polling ====================

    private void pollLoop(String channel, int pollTimeoutSeconds,
                          int maxMessages, int visibilitySeconds) {
        log.debug("Starting queue poll loop for channel '{}'", channel);
        while (running.get()) {
            try {
                concurrencySemaphore.acquire();
                try {
                    QueuesPollRequest request = QueuesPollRequest.builder()
                            .channel(channel)
                            .pollMaxMessages(maxMessages)
                            .pollWaitTimeoutInSeconds(pollTimeoutSeconds)
                            .autoAckMessages(endpoint.isAutoAck())
                            .visibilitySeconds(visibilitySeconds)
                            .build();
                    QueuesPollResponse response = queuesClient.receiveQueueMessages(request);
                    if (response != null && response.getMessages() != null
                            && !response.getMessages().isEmpty()) {
                        List<QueueMessageReceived> messages = response.getMessages();
                        if (endpoint.isBatch()) {
                            invokeListenerMethod(messages);
                        } else {
                            for (QueueMessageReceived msg : messages) {
                                invokeListenerMethod(msg);
                            }
                        }
                    }
                    resetBackoff();
                } finally {
                    concurrencySemaphore.release();
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                if (!running.get()) {
                    break;
                }
                log.warn("Queue poll error on channel '{}': {}", channel, e.getMessage(), e);
                handleError(e);
                backoffSleep(channel);
            }
        }
        log.debug("Queue poll loop ended for channel '{}'", channel);
    }

    // ==================== Internal ====================

    private Object invokeListenerMethod(Object message) {
        try {
            if (isSuspend) {
                coroutineSupport.invokeSuspend(endpoint.getId(), endpoint.getBean(),
                        endpoint.getMethod(), message)
                    .whenComplete((v, ex) -> { if (ex != null) handleError(ex); });
                return null;
            }
            Object result = endpoint.getMethod().invoke(endpoint.getBean(), message);
            if (result instanceof CompletableFuture<?> future) {
                future.whenComplete((v, ex) -> {
                    if (ex != null) {
                        log.error("Async listener '{}' completed exceptionally",
                                endpoint.getId(), ex);
                        handleError(ex);
                    }
                });
            }
            return result;
        } catch (InvocationTargetException e) {
            handleError(e.getTargetException());
            return null;
        } catch (IllegalAccessException e) {
            handleError(e);
            return null;
        }
    }

    /**
     * Invokes the listener method and propagates exceptions instead of swallowing them.
     * Used by command/query synchronous paths where the caller needs to catch errors
     * to send error responses back to the broker.
     */
    private Object invokeListenerMethodDirect(Object message) throws Exception {
        try {
            return endpoint.getMethod().invoke(endpoint.getBean(), message);
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof Exception ex) {
                throw ex;
            }
            throw new RuntimeException(target);
        }
    }

    private void handleSubscriptionError(KubeMQException error) {
        log.warn("KubeMQ subscription error on container '{}': {}", endpoint.getId(), error.getMessage());
        handleError(error);
    }

    private void handleError(Throwable cause) {
        if (errorHandler != null) {
            errorHandler.handleError(cause);
        }
    }

    private int effectiveConcurrency() {
        return endpoint.getConcurrency() > 0 ? endpoint.getConcurrency() : 1;
    }

    private void cancelAllSubscriptions() {
        for (Runnable cancel : activeCancellations) {
            try {
                cancel.run();
            } catch (Exception e) {
                log.warn("Error cancelling subscription in container '{}': {}",
                        endpoint.getId(), e.getMessage());
            }
        }
        activeCancellations.clear();
    }

    private void backoffSleep(String channel) {
        long delay = currentBackoffMs.get();
        log.debug("Backing off {}ms before retrying poll on channel '{}'", delay, channel);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        currentBackoffMs.set(Math.min((long) (delay * backoffMultiplier), maxBackoffMs));
    }

    private void resetBackoff() {
        currentBackoffMs.set(initialBackoffMs);
    }

    private void shutdownQueuePollExecutor() {
        if (queuePollExecutor != null) {
            queuePollExecutor.shutdown();
            try {
                if (!queuePollExecutor.awaitTermination(
                        shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                    queuePollExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                queuePollExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
