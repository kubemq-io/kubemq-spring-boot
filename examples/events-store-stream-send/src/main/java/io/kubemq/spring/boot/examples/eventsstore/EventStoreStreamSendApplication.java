package io.kubemq.spring.boot.examples.eventsstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Event Store Stream Send Example
 *
 * <p>Demonstrates asynchronous persistent event sending using
 * {@code sendEventStoreAsync} with {@code CompletableFuture} for high-throughput scenarios.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends persistent events asynchronously</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class EventStoreStreamSendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventStoreStreamSendApplication.class, args);
    }
}
// Expected output:
// INFO  Async persistent event #1 sent successfully
