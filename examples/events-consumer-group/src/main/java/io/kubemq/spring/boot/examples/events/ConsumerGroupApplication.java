package io.kubemq.spring.boot.examples.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Consumer Group Example
 *
 * <p>Demonstrates load-balanced event subscription using the {@code group} attribute
 * on {@code @KubeMQEventListener}. When multiple listeners share the same group,
 * each event is delivered to only one member of the group.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends events</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventListener} — receives events with group-based load balancing</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class ConsumerGroupApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerGroupApplication.class, args);
    }
}
// Expected output:
// INFO  Sent event #1 to spring-events.consumer-group
