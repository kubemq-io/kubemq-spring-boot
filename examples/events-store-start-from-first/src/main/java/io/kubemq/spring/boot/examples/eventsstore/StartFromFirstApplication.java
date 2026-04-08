package io.kubemq.spring.boot.examples.eventsstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Start From First Example
 *
 * <p>Demonstrates the {@code StartFromFirst} subscription type for events store, where the
 * subscriber replays all stored events from the very first sequence number.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends persistent events</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener} — subscribes with StartFromFirst</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class StartFromFirstApplication {

    public static void main(String[] args) {
        SpringApplication.run(StartFromFirstApplication.class, args);
    }
}
// Expected output:
// INFO  Received (from-first): sequence=1 body=First-replay event #1
