package io.kubemq.spring.boot.examples.eventsstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Replay From Sequence Example
 *
 * <p>Demonstrates the {@code StartAtSequence} subscription type for events store, where the
 * subscriber replays stored events starting from a specific sequence number.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends persistent events</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener} — subscribes with StartAtSequence</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class ReplayFromSequenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReplayFromSequenceApplication.class, args);
    }
}
// Expected output:
// INFO  Replayed from sequence: sequence=5 body=Sequenced event #5
