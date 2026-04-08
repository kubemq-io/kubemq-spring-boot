package io.kubemq.spring.boot.examples.eventsstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Replay From Time Example
 *
 * <p>Demonstrates the {@code StartAtTime} subscription type for events store, where the
 * subscriber replays stored events from a specific point in time (epoch seconds).
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends persistent events</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener} — subscribes with StartAtTime</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class ReplayFromTimeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReplayFromTimeApplication.class, args);
    }
}
// Expected output:
// INFO  Replayed from time: sequence=1 body=Time-replay event #1
