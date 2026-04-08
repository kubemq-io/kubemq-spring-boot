package io.kubemq.spring.boot.examples.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Wildcard Subscribe Example
 *
 * <p>Demonstrates wildcard channel patterns with {@code @KubeMQEventListener}.
 * Subscribers can use {@code *} (single level) and {@code >} (multi-level) wildcards.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends to specific channels</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventListener} — subscribes with wildcard pattern</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class WildcardSubscribeApplication {

    public static void main(String[] args) {
        SpringApplication.run(WildcardSubscribeApplication.class, args);
    }
}
// Expected output:
// INFO  Sent event to spring-events.orders.us
