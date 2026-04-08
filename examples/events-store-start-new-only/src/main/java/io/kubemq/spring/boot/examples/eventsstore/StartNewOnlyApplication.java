package io.kubemq.spring.boot.examples.eventsstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Start New Only Example
 *
 * <p>Demonstrates the {@code StartNewOnly} subscription type for events store, where the
 * subscriber only receives events published after the subscription starts.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends persistent events</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener} — subscribes with StartNewOnly</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class StartNewOnlyApplication {

    public static void main(String[] args) {
        SpringApplication.run(StartNewOnlyApplication.class, args);
    }
}
// Expected output:
// INFO  Received (new-only): sequence=1 body=New-only event #1
