package io.kubemq.spring.boot.examples.eventsstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Event Store Cancel Subscription Example
 *
 * <p>Demonstrates programmatic subscription lifecycle management for persistent events —
 * creating a subscription via the SDK client, receiving events, and cancelling the subscription
 * at runtime to stop delivery.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends persistent events</li>
 *   <li>{@link io.kubemq.sdk.pubsub.PubSubClient} — programmatic subscription and cancellation</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class EventStoreCancelSubscriptionApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventStoreCancelSubscriptionApplication.class, args);
    }
}
// Expected output:
// INFO  Events store subscription started on spring-events-store.cancel-sub
