package io.kubemq.spring.boot.examples.eventsstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Start From Last Example
 *
 * <p>Demonstrates the {@code StartFromLast} subscription type for events store, where the
 * subscriber receives only the most recently stored event and then new events going forward.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends persistent events</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener} — subscribes with StartFromLast</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class StartFromLastApplication {

    public static void main(String[] args) {
        SpringApplication.run(StartFromLastApplication.class, args);
    }
}
// Expected output:
// INFO  Received (from-last): sequence=5 body=Last-replay event #5
