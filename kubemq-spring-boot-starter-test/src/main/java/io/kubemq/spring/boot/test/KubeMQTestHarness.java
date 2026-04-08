package io.kubemq.spring.boot.test;

import kubemq.Kubemq;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

/**
 * Fluent assertion API for verifying KubeMQ message interactions in tests.
 *
 * <p>Inspired by MassTransit's test harness, provides async-aware assertions
 * that poll the {@link MockKubeMQService} until conditions are met or timeout.
 *
 * <p>Usage:
 * <pre>{@code
 * KubeMQTestHarness harness = new KubeMQTestHarness(mockServer.getMockService());
 * harness.expectEvent()
 *     .onChannel("events.orders")
 *     .withBodyContaining("order-123")
 *     .receivedWithin(Duration.ofSeconds(5));
 * }</pre>
 */
public class KubeMQTestHarness {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofMillis(100);

    private final MockKubeMQService mockService;

    public KubeMQTestHarness(MockKubeMQService mockService) {
        this.mockService = mockService;
    }

    /**
     * Returns a builder for asserting events were received by the mock service.
     */
    public EventAssertion expectEvent() {
        return new EventAssertion();
    }

    /**
     * Returns a builder for asserting command requests were received.
     */
    public RequestAssertion expectCommand() {
        return new RequestAssertion(Kubemq.Request.RequestType.Command);
    }

    /**
     * Returns a builder for asserting query requests were received.
     */
    public RequestAssertion expectQuery() {
        return new RequestAssertion(Kubemq.Request.RequestType.Query);
    }

    /**
     * Returns a builder for asserting queue messages were received.
     */
    public QueueAssertion expectQueueMessage() {
        return new QueueAssertion();
    }

    /**
     * Asserts that no events were received on any channel within the given duration.
     */
    public void expectNoEvents(Duration within) {
        Awaitility.await()
                .during(within)
                .atMost(within.plusMillis(500))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    List<Kubemq.Event> events = mockService.getReceivedEvents();
                    if (!events.isEmpty()) {
                        throw new AssertionError(
                                "Expected no events but received " + events.size());
                    }
                });
    }

    /**
     * Resets the mock service, clearing all captured messages.
     */
    public void reset() {
        mockService.reset();
    }

    public class EventAssertion {
        private String channel;
        private Predicate<Kubemq.Event> predicate = e -> true;

        public EventAssertion onChannel(String channel) {
            this.channel = channel;
            return this;
        }

        public EventAssertion matching(Predicate<Kubemq.Event> predicate) {
            this.predicate = predicate;
            return this;
        }

        public EventAssertion withBodyContaining(String substring) {
            this.predicate = predicate.and(e ->
                    e.getBody().toStringUtf8().contains(substring));
            return this;
        }

        public void receivedWithin(Duration timeout) {
            Predicate<Kubemq.Event> filter = predicate;
            if (channel != null) {
                filter = filter.and(e -> channel.equals(e.getChannel()));
            }
            Predicate<Kubemq.Event> finalFilter = filter;
            Awaitility.await()
                    .atMost(timeout)
                    .pollInterval(DEFAULT_POLL_INTERVAL)
                    .untilAsserted(() -> {
                        List<Kubemq.Event> events = mockService.getReceivedEvents();
                        boolean found = events.stream().anyMatch(finalFilter);
                        if (!found) {
                            throw new AssertionError(
                                    "Expected event not received within " + timeout +
                                            "; received " + events.size() + " events");
                        }
                    });
        }

        public void received() {
            receivedWithin(DEFAULT_TIMEOUT);
        }
    }

    public class RequestAssertion {
        private final Kubemq.Request.RequestType type;
        private String channel;
        private Predicate<Kubemq.Request> predicate = r -> true;

        RequestAssertion(Kubemq.Request.RequestType type) {
            this.type = type;
        }

        public RequestAssertion onChannel(String channel) {
            this.channel = channel;
            return this;
        }

        public RequestAssertion matching(Predicate<Kubemq.Request> predicate) {
            this.predicate = predicate;
            return this;
        }

        public void receivedWithin(Duration timeout) {
            Predicate<Kubemq.Request> filter = predicate
                    .and(r -> r.getRequestTypeData() == type);
            if (channel != null) {
                filter = filter.and(r -> channel.equals(r.getChannel()));
            }
            Predicate<Kubemq.Request> finalFilter = filter;
            Awaitility.await()
                    .atMost(timeout)
                    .pollInterval(DEFAULT_POLL_INTERVAL)
                    .untilAsserted(() -> {
                        List<Kubemq.Request> requests = mockService.getReceivedRequests();
                        boolean found = requests.stream().anyMatch(finalFilter);
                        if (!found) {
                            throw new AssertionError(
                                    "Expected " + type + " request not received within " + timeout +
                                            "; received " + requests.size() + " requests");
                        }
                    });
        }

        public void received() {
            receivedWithin(DEFAULT_TIMEOUT);
        }
    }

    public class QueueAssertion {
        private String channel;
        private Predicate<Kubemq.QueueMessage> predicate = q -> true;

        public QueueAssertion onChannel(String channel) {
            this.channel = channel;
            return this;
        }

        public QueueAssertion matching(Predicate<Kubemq.QueueMessage> predicate) {
            this.predicate = predicate;
            return this;
        }

        public QueueAssertion withBodyContaining(String substring) {
            this.predicate = predicate.and(q ->
                    q.getBody().toStringUtf8().contains(substring));
            return this;
        }

        public void receivedWithin(Duration timeout) {
            Predicate<Kubemq.QueueMessage> filter = predicate;
            if (channel != null) {
                filter = filter.and(q -> channel.equals(q.getChannel()));
            }
            Predicate<Kubemq.QueueMessage> finalFilter = filter;
            Awaitility.await()
                    .atMost(timeout)
                    .pollInterval(DEFAULT_POLL_INTERVAL)
                    .untilAsserted(() -> {
                        List<Kubemq.QueueMessage> messages = mockService.getReceivedQueueMessages();
                        boolean found = messages.stream().anyMatch(finalFilter);
                        if (!found) {
                            throw new AssertionError(
                                    "Expected queue message not received within " + timeout +
                                            "; received " + messages.size() + " messages");
                        }
                    });
        }

        public void received() {
            receivedWithin(DEFAULT_TIMEOUT);
        }
    }
}
