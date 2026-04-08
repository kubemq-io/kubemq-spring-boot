package io.kubemq.spring.boot.autoconfigure.template;

import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.cq.CommandMessage;
import io.kubemq.sdk.cq.CommandResponseMessage;
import io.kubemq.sdk.cq.QueryMessage;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.sdk.pubsub.EventMessage;
import io.kubemq.sdk.pubsub.EventStoreMessage;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueueMessage;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter;
import io.kubemq.spring.boot.autoconfigure.observation.DefaultKubeMQObservationConvention;
import io.kubemq.spring.boot.autoconfigure.observation.KubeMQObservationConvention;
import io.kubemq.spring.boot.autoconfigure.observation.KubeMQSendObservation;
import io.kubemq.spring.boot.autoconfigure.observation.KubeMQSendObservationContext;
import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import java.nio.charset.StandardCharsets;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.concurrent.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe template for sending messages to KubeMQ across all five messaging patterns.
 *
 * <p>Wraps the Java SDK clients ({@link PubSubClient}, {@link QueuesClient}, {@link CQClient})
 * with serialization via {@link KubeMQMessageConverter} and optional Micrometer Observation.
 */
@ThreadSafe
public class KubeMQTemplate {

    private static final Logger log = LoggerFactory.getLogger(KubeMQTemplate.class);

    private final PubSubClient pubSubClient;
    private final QueuesClient queuesClient;
    private final CQClient cqClient;
    private final KubeMQProperties properties;
    private volatile KubeMQMessageConverter messageConverter;
    private final ObservationRegistry observationRegistry;
    private final KubeMQObservationConvention observationConvention;
    private final DefaultKubeMQObservationConvention defaultObservationConvention;
    private final boolean observationEnabled;

    public KubeMQTemplate(
            PubSubClient pubSubClient,
            QueuesClient queuesClient,
            CQClient cqClient,
            KubeMQMessageConverter messageConverter,
            ObservationRegistry observationRegistry,
            KubeMQObservationConvention observationConvention,
            KubeMQProperties properties) {
        this.pubSubClient = Objects.requireNonNull(pubSubClient, "pubSubClient must not be null");
        this.queuesClient = Objects.requireNonNull(queuesClient, "queuesClient must not be null");
        this.cqClient = Objects.requireNonNull(cqClient, "cqClient must not be null");
        this.messageConverter = messageConverter;
        this.observationRegistry = observationRegistry;
        this.observationConvention = observationConvention;
        this.defaultObservationConvention = new DefaultKubeMQObservationConvention();
        this.observationEnabled = observationRegistry != null
                && properties.getTemplate().isObservationEnabled();
        this.properties = properties;
    }

    // ==================== Events ====================

    public void sendEvent(String channel, Object data) {
        sendEvent(channel, data, Collections.emptyMap());
    }

    public void sendEvent(String channel, Object data, Map<String, String> tags) {
        Map<String, String> mutableTags = new HashMap<>(tags);
        byte[] body = serializePayload(data, mutableTags);
        EventMessage message = EventMessage.builder()
                .channel(channel)
                .body(body)
                .tags(mutableTags)
                .build();
        log.debug("Sending event to channel '{}'", channel);
        if (observationEnabled) {
            createObservation(channel, "EVENTS")
                    .observe(() -> pubSubClient.publishEvent(message));
        } else {
            pubSubClient.publishEvent(message);
        }
    }

    public CompletableFuture<Void> sendEventAsync(String channel, Object data) {
        return sendEventAsync(channel, data, Collections.emptyMap());
    }

    public CompletableFuture<Void> sendEventAsync(String channel, Object data, Map<String, String> tags) {
        Map<String, String> mutableTags = new HashMap<>(tags);
        byte[] body = serializePayload(data, mutableTags);
        EventMessage message = EventMessage.builder()
                .channel(channel)
                .body(body)
                .tags(mutableTags)
                .build();
        log.debug("Sending event async to channel '{}'", channel);
        if (observationEnabled) {
            Observation observation = createObservation(channel, "EVENTS");
            observation.start();
            Observation.Scope scope = observation.openScope();
            try {
                return pubSubClient.sendEventsMessageAsync(message)
                        .whenComplete((result, error) -> {
                            if (error != null) observation.error(error);
                            scope.close();
                            observation.stop();
                        });
            } catch (Exception e) {
                observation.error(e);
                scope.close();
                observation.stop();
                throw e;
            }
        }
        return pubSubClient.sendEventsMessageAsync(message);
    }

    // ==================== Events Store ====================

    public void sendEventStore(String channel, Object data) {
        sendEventStore(channel, data, Collections.emptyMap());
    }

    public void sendEventStore(String channel, Object data, Map<String, String> tags) {
        Map<String, String> mutableTags = new HashMap<>(tags);
        byte[] body = serializePayload(data, mutableTags);
        EventStoreMessage message = EventStoreMessage.builder()
                .channel(channel)
                .body(body)
                .tags(mutableTags)
                .build();
        log.debug("Sending event store to channel '{}'", channel);
        if (observationEnabled) {
            createObservation(channel, "EVENTS_STORE")
                    .observe(() -> pubSubClient.publishEventStore(message));
        } else {
            pubSubClient.publishEventStore(message);
        }
    }

    public CompletableFuture<Void> sendEventStoreAsync(String channel, Object data) {
        return sendEventStoreAsync(channel, data, Collections.emptyMap());
    }

    public CompletableFuture<Void> sendEventStoreAsync(String channel, Object data, Map<String, String> tags) {
        Map<String, String> mutableTags = new HashMap<>(tags);
        byte[] body = serializePayload(data, mutableTags);
        EventStoreMessage message = EventStoreMessage.builder()
                .channel(channel)
                .body(body)
                .tags(mutableTags)
                .build();
        log.debug("Sending event store async to channel '{}'", channel);
        if (observationEnabled) {
            Observation observation = createObservation(channel, "EVENTS_STORE");
            observation.start();
            Observation.Scope scope = observation.openScope();
            try {
                return pubSubClient.sendEventsStoreMessageAsync(message)
                        .thenApply(result -> (Void) null)
                        .whenComplete((result, error) -> {
                            if (error != null) observation.error(error);
                            scope.close();
                            observation.stop();
                        });
            } catch (Exception e) {
                observation.error(e);
                scope.close();
                observation.stop();
                throw e;
            }
        }
        return pubSubClient.sendEventsStoreMessageAsync(message)
                .thenApply(result -> null);
    }

    // ==================== Queues ====================

    public void sendQueueMessage(String channel, Object data) {
        sendQueueMessage(channel, data, Collections.emptyMap());
    }

    public void sendQueueMessage(String channel, Object data, Map<String, String> tags) {
        Map<String, String> mutableTags = new HashMap<>(tags);
        byte[] body = serializePayload(data, mutableTags);
        QueueMessage message = QueueMessage.builder()
                .channel(channel)
                .body(body)
                .tags(mutableTags)
                .build();
        log.debug("Sending queue message to channel '{}'", channel);
        if (observationEnabled) {
            createObservation(channel, "QUEUES")
                    .observe(() -> queuesClient.sendQueueMessage(message));
        } else {
            queuesClient.sendQueueMessage(message);
        }
    }

    public CompletableFuture<Void> sendQueueMessageAsync(String channel, Object data) {
        return sendQueueMessageAsync(channel, data, Collections.emptyMap());
    }

    public CompletableFuture<Void> sendQueueMessageAsync(String channel, Object data, Map<String, String> tags) {
        Map<String, String> mutableTags = new HashMap<>(tags);
        byte[] body = serializePayload(data, mutableTags);
        QueueMessage message = QueueMessage.builder()
                .channel(channel)
                .body(body)
                .tags(mutableTags)
                .build();
        log.debug("Sending queue message async to channel '{}'", channel);
        if (observationEnabled) {
            Observation observation = createObservation(channel, "QUEUES");
            observation.start();
            Observation.Scope scope = observation.openScope();
            try {
                return queuesClient.sendQueuesMessageAsync(message)
                        .thenApply(result -> (Void) null)
                        .whenComplete((result, error) -> {
                            if (error != null) observation.error(error);
                            scope.close();
                            observation.stop();
                        });
            } catch (Exception e) {
                observation.error(e);
                scope.close();
                observation.stop();
                throw e;
            }
        }
        return queuesClient.sendQueuesMessageAsync(message)
                .thenApply(result -> null);
    }

    /**
     * Sends multiple queue messages to the same channel. Messages are pre-built
     * in a batch before being sent, ensuring consistent serialization and
     * reducing per-message overhead.
     *
     * <p>When the Java SDK adds a batch {@code sendQueuesMessages(List)} method,
     * this implementation should be updated to use a single RPC call.
     */
    public void sendQueueMessages(String channel, List<?> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        // Pre-build all messages before sending to fail-fast on serialization errors
        List<QueueMessage> messages = new ArrayList<>(data.size());
        for (Object item : data) {
            Map<String, String> mutableTags = new HashMap<>();
            byte[] body = serializePayload(item, mutableTags);
            messages.add(QueueMessage.builder()
                    .channel(channel)
                    .body(body)
                    .tags(mutableTags)
                    .build());
        }
        log.debug("Sending {} queue messages to channel '{}'", messages.size(), channel);
        if (observationEnabled) {
            createObservation(channel, "QUEUES").observe(() -> {
                for (QueueMessage msg : messages) {
                    queuesClient.sendQueuesMessage(msg);
                }
            });
        } else {
            for (QueueMessage msg : messages) {
                queuesClient.sendQueuesMessage(msg);
            }
        }
    }

    // ==================== Commands ====================

    @SuppressWarnings("deprecation")
    public CommandResponseMessage sendCommand(String channel, Object data, Duration timeout) {
        Map<String, String> tags = new HashMap<>();
        byte[] body = serializePayload(data, tags);
        CommandMessage message = CommandMessage.builder()
                .channel(channel)
                .body(body)
                .tags(tags)
                .timeoutInSeconds(durationToSeconds(timeout))
                .build();
        log.debug("Sending command to channel '{}' with timeout {}", channel, timeout);
        if (observationEnabled) {
            return createObservation(channel, "COMMANDS")
                    .observe(() -> cqClient.sendCommandRequest(message));
        }
        return cqClient.sendCommandRequest(message);
    }

    @SuppressWarnings("deprecation")
    public CompletableFuture<CommandResponseMessage> sendCommandAsync(
            String channel, Object data, Duration timeout) {
        Map<String, String> tags = new HashMap<>();
        byte[] body = serializePayload(data, tags);
        CommandMessage message = CommandMessage.builder()
                .channel(channel)
                .body(body)
                .tags(tags)
                .timeoutInSeconds(durationToSeconds(timeout))
                .build();
        log.debug("Sending command async to channel '{}' with timeout {}", channel, timeout);
        if (observationEnabled) {
            Observation observation = createObservation(channel, "COMMANDS");
            observation.start();
            Observation.Scope scope = observation.openScope();
            try {
                return cqClient.sendCommandRequestAsync(message)
                        .whenComplete((result, error) -> {
                            if (error != null) observation.error(error);
                            scope.close();
                            observation.stop();
                        });
            } catch (Exception e) {
                observation.error(e);
                scope.close();
                observation.stop();
                throw e;
            }
        }
        return cqClient.sendCommandRequestAsync(message);
    }

    // ==================== Queries ====================

    @SuppressWarnings("deprecation")
    public QueryResponseMessage sendQuery(String channel, Object data, Duration timeout) {
        Map<String, String> tags = new HashMap<>();
        byte[] body = serializePayload(data, tags);
        QueryMessage message = QueryMessage.builder()
                .channel(channel)
                .body(body)
                .tags(tags)
                .timeoutInSeconds(durationToSeconds(timeout))
                .build();
        log.debug("Sending query to channel '{}' with timeout {}", channel, timeout);
        if (observationEnabled) {
            return createObservation(channel, "QUERIES")
                    .observe(() -> cqClient.sendQueryRequest(message));
        }
        return cqClient.sendQueryRequest(message);
    }

    @SuppressWarnings("deprecation")
    public CompletableFuture<QueryResponseMessage> sendQueryAsync(
            String channel, Object data, Duration timeout) {
        Map<String, String> tags = new HashMap<>();
        byte[] body = serializePayload(data, tags);
        QueryMessage message = QueryMessage.builder()
                .channel(channel)
                .body(body)
                .tags(tags)
                .timeoutInSeconds(durationToSeconds(timeout))
                .build();
        log.debug("Sending query async to channel '{}' with timeout {}", channel, timeout);
        if (observationEnabled) {
            Observation observation = createObservation(channel, "QUERIES");
            observation.start();
            Observation.Scope scope = observation.openScope();
            try {
                return cqClient.sendQueryRequestAsync(message)
                        .whenComplete((result, error) -> {
                            if (error != null) observation.error(error);
                            scope.close();
                            observation.stop();
                        });
            } catch (Exception e) {
                observation.error(e);
                scope.close();
                observation.stop();
                throw e;
            }
        }
        return cqClient.sendQueryRequestAsync(message);
    }

    // ==================== Fluent Builders ====================

    public KubeMQEventMessageBuilder newEvent(Object data) {
        return new KubeMQEventMessageBuilder(this, data, false);
    }

    public KubeMQEventMessageBuilder newEventStore(Object data) {
        return new KubeMQEventMessageBuilder(this, data, true);
    }

    public KubeMQQueueMessageBuilder newQueueMessage(Object data) {
        return new KubeMQQueueMessageBuilder(this, data);
    }

    public KubeMQCommandMessageBuilder newCommand(Object data) {
        return new KubeMQCommandMessageBuilder(this, data);
    }

    public KubeMQQueryMessageBuilder newQuery(Object data) {
        return new KubeMQQueryMessageBuilder(this, data);
    }

    // ==================== Converter Access ====================

    public void setMessageConverter(KubeMQMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public KubeMQMessageConverter getMessageConverter() {
        return messageConverter;
    }

    // ==================== Package-Private Accessors for Builders ====================

    PubSubClient getPubSubClient() {
        return pubSubClient;
    }

    QueuesClient getQueuesClient() {
        return queuesClient;
    }

    CQClient getCqClient() {
        return cqClient;
    }

    // ==================== Internal Helpers ====================

    private Observation createObservation(String channel, String pattern) {
        KubeMQSendObservationContext ctx = new KubeMQSendObservationContext(channel, pattern);
        return KubeMQSendObservation.SEND.observation(
                observationConvention, defaultObservationConvention,
                () -> ctx, observationRegistry);
    }

    private byte[] serializePayload(Object data, Map<String, String> tags) {
        KubeMQMessageConverter converter = this.messageConverter; // volatile snapshot
        if (converter != null) {
            return converter.toBytes(data, tags);
        }
        return defaultSerialize(data);
    }

    /**
     * Fallback serialization when no {@link KubeMQMessageConverter} is configured.
     * Handles {@code byte[]} and {@code String} payloads; throws for all other types.
     */
    static byte[] defaultSerialize(Object data) {
        if (data instanceof byte[] bytes) {
            return bytes;
        }
        if (data instanceof String str) {
            return str.getBytes(StandardCharsets.UTF_8);
        }
        throw new IllegalArgumentException(
                "No KubeMQMessageConverter configured and payload is not byte[] or String: "
                        + data.getClass().getName());
    }

    static int durationToSeconds(Duration timeout) {
        if (timeout == null || timeout.isZero()) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(timeout.toMillis() / 1000.0));
    }
}
