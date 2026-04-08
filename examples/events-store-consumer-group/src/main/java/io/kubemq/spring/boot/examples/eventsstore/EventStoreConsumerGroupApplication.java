package io.kubemq.spring.boot.examples.eventsstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Event Store Consumer Group Example
 *
 * <p>Demonstrates load-balanced persistent event consumption using consumer groups. Two listener
 * beans share the same {@code group} so each event is delivered to only one listener.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends persistent events</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener} — load-balanced consumption via consumer groups</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class EventStoreConsumerGroupApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventStoreConsumerGroupApplication.class, args);
    }
}
// Expected output:
// INFO  Sent persistent event #1 to spring-events-store.consumer-group
