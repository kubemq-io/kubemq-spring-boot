package io.kubemq.spring.boot.examples.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Cancel Subscription Example
 *
 * <p>Demonstrates programmatic subscription creation and cancellation using
 * the low-level {@link io.kubemq.sdk.pubsub.PubSubClient} and
 * {@link io.kubemq.sdk.pubsub.EventsSubscription} APIs.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends events</li>
 *   <li>{@link io.kubemq.sdk.pubsub.PubSubClient} — programmatic subscribe/cancel</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class CancelSubscriptionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CancelSubscriptionApplication.class, args);
    }
}
// Expected output:
// INFO  Subscription started on spring-events.cancel-sub
