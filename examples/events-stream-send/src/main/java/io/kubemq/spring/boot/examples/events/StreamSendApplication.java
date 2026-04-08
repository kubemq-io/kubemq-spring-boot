package io.kubemq.spring.boot.examples.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Stream Send Example
 *
 * <p>Demonstrates asynchronous (non-blocking) event publishing using
 * {@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate#sendEventAsync}.
 * Uses {@link java.util.concurrent.CompletableFuture} to track completion of each send.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — async event sending</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class StreamSendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamSendApplication.class, args);
    }
}
// Expected output:
// INFO  Async event #1 sent successfully
