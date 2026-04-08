package io.kubemq.spring.boot.examples.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Multiple Subscribers Example
 *
 * <p>Demonstrates fan-out event delivery where multiple independent subscribers
 * each receive a copy of every published event.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends events</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventListener} — multiple independent listeners</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class MultipleSubscribersApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultipleSubscribersApplication.class, args);
    }
}
// Expected output:
// INFO  Sent broadcast event to spring-events.multi-sub
