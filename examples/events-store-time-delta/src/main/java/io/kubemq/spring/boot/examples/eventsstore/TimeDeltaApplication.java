package io.kubemq.spring.boot.examples.eventsstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Time Delta Example
 *
 * <p>Demonstrates the {@code StartAtTimeDelta} subscription type for events store, where the
 * subscriber replays stored events from a relative time offset (e.g., last 60 seconds).
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends persistent events</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener} — subscribes with StartAtTimeDelta</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class TimeDeltaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeDeltaApplication.class, args);
    }
}
// Expected output:
// INFO  Received (time-delta 60s): sequence=1 body=Delta event #1
