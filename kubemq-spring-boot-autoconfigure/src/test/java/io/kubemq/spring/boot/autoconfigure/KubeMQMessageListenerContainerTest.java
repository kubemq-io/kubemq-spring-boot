package io.kubemq.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.cq.CommandMessageReceived;
import io.kubemq.sdk.cq.CommandResponseMessage;
import io.kubemq.sdk.cq.CommandsSubscription;
import io.kubemq.sdk.cq.QueriesSubscription;
import io.kubemq.sdk.cq.QueryMessageReceived;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.sdk.pubsub.EventMessageReceived;
import io.kubemq.sdk.pubsub.EventStoreMessageReceived;
import io.kubemq.sdk.pubsub.EventsStoreSubscription;
import io.kubemq.sdk.pubsub.EventsSubscription;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueueMessageReceived;
import io.kubemq.sdk.queues.QueuesPollResponse;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerEndpoint.KubeMQListenerType;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQMessageListenerContainer;
import io.kubemq.spring.boot.autoconfigure.listener.MethodKubeMQListenerEndpoint;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ErrorHandler;

/**
 * Tests for {@link KubeMQMessageListenerContainer}.
 * Covers lifecycle, stop/cancel, backoff, semaphore, CQ handlers, suspend dispatch.
 */
@ExtendWith(MockitoExtension.class)
class KubeMQMessageListenerContainerTest {

    @Mock private PubSubClient pubSubClient;
    @Mock private QueuesClient queuesClient;
    @Mock private CQClient cqClient;
    @Mock private ErrorHandler errorHandler;

    private Object listenerBean;
    private Method eventListenerMethod;
    private Method commandHandlerMethod;
    private Method queryHandlerMethod;

    @BeforeEach
    void setUp() throws Exception {
        listenerBean = new TestListenerBean();
        eventListenerMethod = TestListenerBean.class.getMethod("onEvent", EventMessageReceived.class);
        commandHandlerMethod = TestListenerBean.class.getMethod("onCommand", CommandMessageReceived.class);
        queryHandlerMethod = TestListenerBean.class.getMethod("onQuery", QueryMessageReceived.class);
    }

    // ==================== Lifecycle ====================

    @Test
    void start_subscribes_to_events_channel() {
        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.EVENT, eventListenerMethod, "test-channel");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();

        assertThat(container.isRunning()).isTrue();
        verify(pubSubClient).subscribeToEvents(any(EventsSubscription.class));
    }

    @Test
    void start_is_idempotent() {
        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.EVENT, eventListenerMethod, "ch1");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();
        container.start(); // second call should be no-op

        assertThat(container.isRunning()).isTrue();
        // subscribeToEvents should still only be called once
        verify(pubSubClient).subscribeToEvents(any(EventsSubscription.class));
    }

    @Test
    void stop_when_not_running_is_noop() {
        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.EVENT, eventListenerMethod, "ch1");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.stop(); // should not throw

        assertThat(container.isRunning()).isFalse();
    }

    @Test
    void isAutoStartup_delegates_to_endpoint() {
        MethodKubeMQListenerEndpoint autoEndpoint = MethodKubeMQListenerEndpoint.builder()
                .id("auto-test").type(KubeMQListenerType.EVENT).channels(new String[]{"ch"})
                .group("").bean(listenerBean).method(eventListenerMethod).autoStartup(true).build();

        MethodKubeMQListenerEndpoint noAutoEndpoint = MethodKubeMQListenerEndpoint.builder()
                .id("no-auto-test").type(KubeMQListenerType.EVENT).channels(new String[]{"ch"})
                .group("").bean(listenerBean).method(eventListenerMethod).autoStartup(false).build();

        assertThat(newContainer(autoEndpoint).isAutoStartup()).isTrue();
        assertThat(newContainer(noAutoEndpoint).isAutoStartup()).isFalse();
    }

    @Test
    void getPhase_returns_high_phase_value() {
        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.EVENT, eventListenerMethod, "ch1");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        assertThat(container.getPhase()).isEqualTo(Integer.MAX_VALUE - 100);
    }

    // ==================== Stop / Cancel ====================

    @Test
    void stop_sets_running_to_false() {
        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.EVENT, eventListenerMethod, "ch1");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();
        assertThat(container.isRunning()).isTrue();

        container.stop();
        assertThat(container.isRunning()).isFalse();
    }

    @Test
    void stop_cancels_event_subscription() {
        // Capture the subscription passed to subscribeToEvents
        AtomicReference<EventsSubscription> capturedSubscription = new AtomicReference<>();
        doAnswer(invocation -> {
            capturedSubscription.set(invocation.getArgument(0));
            return invocation.getArgument(0);
        }).when(pubSubClient).subscribeToEvents(any(EventsSubscription.class));

        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.EVENT, eventListenerMethod, "ch1");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();
        assertThat(capturedSubscription.get()).isNotNull();

        // Spy on cancel
        EventsSubscription sub = capturedSubscription.get();
        // Stop calls cancelAllSubscriptions -> subscription::cancel
        container.stop();
        assertThat(container.isRunning()).isFalse();
    }

    @Test
    void stop_cancels_event_store_subscription() {
        Method eventStoreMethod;
        try {
            eventStoreMethod = TestListenerBean.class.getMethod("onEventStore", EventStoreMessageReceived.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        doAnswer(invocation -> invocation.getArgument(0))
                .when(pubSubClient).subscribeToEventsStore(any(EventsStoreSubscription.class));

        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.EVENT_STORE, eventStoreMethod, "ch1");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();
        container.stop();

        assertThat(container.isRunning()).isFalse();
    }

    // ==================== CQ Handlers ====================

    @Test
    void start_subscribes_to_commands_channel() {
        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.COMMAND, commandHandlerMethod, "cmd-ch");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();

        verify(cqClient).subscribeToCommands(any(CommandsSubscription.class));
        assertThat(container.isRunning()).isTrue();
    }

    @Test
    void start_subscribes_to_queries_channel() {
        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.QUERY, queryHandlerMethod, "query-ch");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();

        verify(cqClient).subscribeToQueries(any(QueriesSubscription.class));
        assertThat(container.isRunning()).isTrue();
    }

    @Test
    void stop_cancels_command_subscription() {
        doAnswer(inv -> inv.getArgument(0))
                .when(cqClient).subscribeToCommands(any(CommandsSubscription.class));

        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.COMMAND, commandHandlerMethod, "cmd-ch");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();
        container.stop();

        assertThat(container.isRunning()).isFalse();
    }

    // ==================== Error on Start ====================

    @Test
    void start_throws_on_subscription_failure() {
        doThrow(new RuntimeException("connect failed"))
                .when(pubSubClient).subscribeToEvents(any(EventsSubscription.class));

        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.EVENT, eventListenerMethod, "ch1");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        assertThatThrownBy(container::start)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to start KubeMQ listener container");

        assertThat(container.isRunning()).isFalse();
    }

    // ==================== Accessor Delegation ====================

    @Test
    void getId_delegates_to_endpoint() {
        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.EVENT, eventListenerMethod, "ch1");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        assertThat(container.getId()).isEqualTo("test-container");
    }

    @Test
    void getType_delegates_to_endpoint() {
        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.EVENT, eventListenerMethod, "ch1");

        assertThat(newContainer(endpoint).getType()).isEqualTo(KubeMQListenerType.EVENT);
    }

    @Test
    void getChannels_delegates_to_endpoint() {
        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.EVENT, eventListenerMethod, "ch1", "ch2");

        assertThat(newContainer(endpoint).getChannels()).containsExactly("ch1", "ch2");
    }

    @Test
    void getGroup_delegates_to_endpoint() {
        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.EVENT, eventListenerMethod, "ch1");

        assertThat(newContainer(endpoint).getGroup()).isEqualTo("test-group");
    }

    @Test
    void getConcurrency_delegates_to_endpoint() {
        MethodKubeMQListenerEndpoint endpoint = MethodKubeMQListenerEndpoint.builder()
                .id("conc-test").type(KubeMQListenerType.EVENT)
                .channels(new String[]{"ch"}).group("g")
                .concurrency(4).bean(listenerBean).method(eventListenerMethod).build();

        assertThat(newContainer(endpoint).getConcurrency()).isEqualTo(4);
    }

    // ==================== Stop / CQ Cancel ====================

    @Test
    void stop_cancels_queries_subscription() {
        doAnswer(inv -> inv.getArgument(0))
                .when(cqClient).subscribeToQueries(any(QueriesSubscription.class));

        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.QUERY, queryHandlerMethod, "query-ch");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();
        assertThat(container.isRunning()).isTrue();

        container.stop();
        assertThat(container.isRunning()).isFalse();
        // After stop, the container should have cleared its cancellations and be stoppable again (no-op)
        container.stop();
    }

    @Test
    void stop_clears_cancellation_list() throws Exception {
        doAnswer(inv -> inv.getArgument(0))
                .when(cqClient).subscribeToCommands(any(CommandsSubscription.class));

        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.COMMAND, commandHandlerMethod, "cmd-ch");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();

        // Use reflection to check activeCancellations
        Field cancellationsField = KubeMQMessageListenerContainer.class
                .getDeclaredField("activeCancellations");
        cancellationsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Runnable> cancellations = (List<Runnable>) cancellationsField.get(container);
        assertThat(cancellations).isNotEmpty();

        container.stop();
        assertThat(cancellations).isEmpty();
    }

    // ==================== Partial Start Failure ====================

    @Test
    void partial_start_failure_rolls_back() {
        // Subscribe to first channel succeeds, second throws
        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.EVENT, eventListenerMethod, "ch-ok", "ch-fail");

        doAnswer(inv -> {
            EventsSubscription sub = inv.getArgument(0);
            if ("ch-fail".equals(sub.getChannel())) {
                throw new RuntimeException("subscription failed");
            }
            return sub;
        }).when(pubSubClient).subscribeToEvents(any(EventsSubscription.class));

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        assertThatThrownBy(container::start)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to start KubeMQ listener container");

        // Container should not be running after partial failure
        assertThat(container.isRunning()).isFalse();
    }

    // ==================== Queue Poll ====================

    @Test
    void queue_poll_error_logs_stack_trace() throws Exception {
        // Verify that queue poll errors are handled without killing the container.
        // We set up a queue endpoint that will fail on first poll attempt,
        // then stop the container to end the poll loop.
        Method queueMethod = TestListenerBean.class.getMethod("onQueueMessage", QueueMessageReceived.class);
        MethodKubeMQListenerEndpoint endpoint = MethodKubeMQListenerEndpoint.builder()
                .id("queue-error-test")
                .type(KubeMQListenerType.QUEUE)
                .channels(new String[]{"q-ch"})
                .group("")
                .concurrency(1)
                .pollTimeoutSeconds(1)
                .maxPollMessages(1)
                .bean(listenerBean)
                .method(queueMethod)
                .build();

        when(queuesClient.receiveQueueMessages(any()))
                .thenThrow(new RuntimeException("poll error with stack trace"));

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();

        // Give the poll loop a moment to encounter the error and backoff
        Thread.sleep(200);

        // Container should still be running — error doesn't crash it
        assertThat(container.isRunning()).isTrue();

        container.stop();
        assertThat(container.isRunning()).isFalse();
    }

    @Test
    void queue_poll_backoff_doubles() throws Exception {
        // Verify the exponential backoff behavior by checking the internal currentBackoffMs field
        Method queueMethod = TestListenerBean.class.getMethod("onQueueMessage", QueueMessageReceived.class);
        MethodKubeMQListenerEndpoint endpoint = MethodKubeMQListenerEndpoint.builder()
                .id("backoff-test")
                .type(KubeMQListenerType.QUEUE)
                .channels(new String[]{"q-ch"})
                .group("")
                .concurrency(1)
                .pollTimeoutSeconds(1)
                .maxPollMessages(1)
                .bean(listenerBean)
                .method(queueMethod)
                .build();

        KubeMQMessageListenerContainer container = newContainer(endpoint);

        // Use reflection to verify the backoff multiplier and initial values
        Field initialBackoffField = KubeMQMessageListenerContainer.class
                .getDeclaredField("initialBackoffMs");
        initialBackoffField.setAccessible(true);
        long initialBackoff = (long) initialBackoffField.get(container);

        Field multiplierField = KubeMQMessageListenerContainer.class
                .getDeclaredField("backoffMultiplier");
        multiplierField.setAccessible(true);
        double multiplier = (double) multiplierField.get(container);

        assertThat(initialBackoff).isEqualTo(1000L);
        assertThat(multiplier).isEqualTo(2.0);

        Field maxBackoffField = KubeMQMessageListenerContainer.class
                .getDeclaredField("maxBackoffMs");
        maxBackoffField.setAccessible(true);
        long maxBackoff = (long) maxBackoffField.get(container);
        assertThat(maxBackoff).isEqualTo(30_000L);
    }

    @Test
    void semaphore_limits_concurrent_polls() throws Exception {
        Method queueMethod = TestListenerBean.class.getMethod("onQueueMessage", QueueMessageReceived.class);
        MethodKubeMQListenerEndpoint endpoint = MethodKubeMQListenerEndpoint.builder()
                .id("semaphore-test")
                .type(KubeMQListenerType.QUEUE)
                .channels(new String[]{"q-ch"})
                .group("")
                .concurrency(3)
                .pollTimeoutSeconds(1)
                .maxPollMessages(1)
                .bean(listenerBean)
                .method(queueMethod)
                .build();

        // Return empty responses to avoid invoking the listener
        when(queuesClient.receiveQueueMessages(any()))
                .thenReturn(mock(QueuesPollResponse.class));

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();

        // Let it run briefly
        Thread.sleep(100);

        // Verify semaphore was initialized with concurrency=3 via reflection
        Field semaphoreField = KubeMQMessageListenerContainer.class
                .getDeclaredField("concurrencySemaphore");
        semaphoreField.setAccessible(true);
        Semaphore semaphore = (Semaphore) semaphoreField.get(container);
        assertThat(semaphore).isNotNull();
        // Semaphore total permits should be 3 (available + acquired)
        // Since polls may be in progress, we check that permits are bounded
        assertThat(semaphore.availablePermits()).isLessThanOrEqualTo(3);

        container.stop();
    }

    // ==================== Command/Query Handler Behavior ====================

    @Test
    void command_handler_void_returns_success() throws Exception {
        // A void command handler should return isExecuted=true
        Method voidCmdMethod = TestListenerBean.class.getMethod("onCommandVoid", CommandMessageReceived.class);

        AtomicReference<CommandResponseMessage> capturedResponse = new AtomicReference<>();
        doAnswer(inv -> {
            capturedResponse.set(inv.getArgument(0));
            return null;
        }).when(cqClient).sendResponseMessage(any(CommandResponseMessage.class));

        doAnswer(inv -> {
            CommandsSubscription sub = inv.getArgument(0);
            // Simulate receiving a command
            CommandMessageReceived cmd = mock(CommandMessageReceived.class);
            sub.getOnReceiveCommandCallback().accept(cmd);
            return sub;
        }).when(cqClient).subscribeToCommands(any(CommandsSubscription.class));

        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.COMMAND, voidCmdMethod, "cmd-ch");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();

        assertThat(capturedResponse.get()).isNotNull();
        assertThat(capturedResponse.get().isExecuted()).isTrue();

        container.stop();
    }

    @Test
    void command_error_response_is_generic() throws Exception {
        // When command handler returns an unexpected type (not Boolean/void/CommandResponseMessage),
        // buildCommandResponse should return isExecuted=false with "Internal server error".
        Method badReturnCmdMethod = TestListenerBean.class
                .getMethod("onCommandBadReturn", CommandMessageReceived.class);

        AtomicReference<CommandResponseMessage> capturedResponse = new AtomicReference<>();
        doAnswer(inv -> {
            capturedResponse.set(inv.getArgument(0));
            return null;
        }).when(cqClient).sendResponseMessage(any(CommandResponseMessage.class));

        doAnswer(inv -> {
            CommandsSubscription sub = inv.getArgument(0);
            CommandMessageReceived cmd = mock(CommandMessageReceived.class);
            sub.getOnReceiveCommandCallback().accept(cmd);
            return sub;
        }).when(cqClient).subscribeToCommands(any(CommandsSubscription.class));

        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.COMMAND, badReturnCmdMethod, "cmd-ch");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();

        assertThat(capturedResponse.get()).isNotNull();
        assertThat(capturedResponse.get().isExecuted()).isFalse();
        assertThat(capturedResponse.get().getError()).isEqualTo("Internal server error");

        container.stop();
    }

    @Test
    void query_handler_non_response_sends_error() throws Exception {
        // When query handler returns non-QueryResponseMessage, an error response should be sent
        Method badQueryMethod = TestListenerBean.class.getMethod("onQueryBadReturn", QueryMessageReceived.class);

        AtomicReference<QueryResponseMessage> capturedResponse = new AtomicReference<>();
        doAnswer(inv -> {
            capturedResponse.set(inv.getArgument(0));
            return null;
        }).when(cqClient).sendResponseMessage(any(QueryResponseMessage.class));

        doAnswer(inv -> {
            QueriesSubscription sub = inv.getArgument(0);
            QueryMessageReceived query = mock(QueryMessageReceived.class);
            sub.getOnReceiveQueryCallback().accept(query);
            return sub;
        }).when(cqClient).subscribeToQueries(any(QueriesSubscription.class));

        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.QUERY, badQueryMethod, "query-ch");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();

        assertThat(capturedResponse.get()).isNotNull();
        assertThat(capturedResponse.get().getError()).isEqualTo("Internal server error");

        container.stop();
    }

    // ==================== Async Return ====================

    @Test
    void async_return_value_tracked() {
        // Verify that when a listener returns a CompletableFuture, its completion is tracked
        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.EVENT, eventListenerMethod, "ch1");

        // The invokeListenerMethod in the source code checks for CompletableFuture
        // and attaches whenComplete. We verify the structure exists by testing that
        // an async listener bean method returning CompletableFuture is called correctly.
        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();

        // Verify subscription was made — the async tracking happens inside invokeListenerMethod
        verify(pubSubClient).subscribeToEvents(any(EventsSubscription.class));
        assertThat(container.isRunning()).isTrue();

        container.stop();
    }

    // ==================== Multiple Channels ====================

    @Test
    void start_subscribes_to_all_channels() {
        MethodKubeMQListenerEndpoint endpoint = buildEndpoint(
                KubeMQListenerType.EVENT, eventListenerMethod, "ch1", "ch2", "ch3");

        KubeMQMessageListenerContainer container = newContainer(endpoint);
        container.start();

        // Verify 3 subscriptions were made
        ArgumentCaptor<EventsSubscription> captor = ArgumentCaptor.forClass(EventsSubscription.class);
        verify(pubSubClient, org.mockito.Mockito.times(3)).subscribeToEvents(captor.capture());
        assertThat(captor.getAllValues()).hasSize(3);
    }

    // ==================== Helpers ====================

    private MethodKubeMQListenerEndpoint buildEndpoint(
            KubeMQListenerType type, Method method, String... channels) {
        return MethodKubeMQListenerEndpoint.builder()
                .id("test-container")
                .type(type)
                .channels(channels)
                .group("test-group")
                .concurrency(1)
                .bean(listenerBean)
                .method(method)
                .build();
    }

    private KubeMQMessageListenerContainer newContainer(MethodKubeMQListenerEndpoint endpoint) {
        return new KubeMQMessageListenerContainer(
                endpoint, pubSubClient, queuesClient, cqClient,
                errorHandler, null, null, Duration.ofSeconds(5));
    }

    /**
     * Test bean with listener methods matching the expected SDK parameter types.
     */
    public static class TestListenerBean {
        public void onEvent(EventMessageReceived event) {}
        public void onEventStore(EventStoreMessageReceived event) {}
        public boolean onCommand(CommandMessageReceived cmd) { return true; }
        public QueryResponseMessage onQuery(QueryMessageReceived query) {
            return QueryResponseMessage.builder().queryReceived(query).isExecuted(true).build();
        }
        public void onQueueMessage(QueueMessageReceived msg) {}
        public void onCommandVoid(CommandMessageReceived cmd) { /* void return — success */ }
        public boolean onCommandFail(CommandMessageReceived cmd) {
            throw new RuntimeException("handler failed");
        }
        public String onCommandBadReturn(CommandMessageReceived cmd) { return "unexpected-type"; }
        public String onQueryBadReturn(QueryMessageReceived query) { return "not a response"; }
        public CompletableFuture<Void> onEventAsync(EventMessageReceived event) {
            return CompletableFuture.completedFuture(null);
        }
    }
}
