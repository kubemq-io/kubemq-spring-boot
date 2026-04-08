package io.kubemq.spring.boot.examples.queues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Queue Send/Receive Example
 *
 * <p>Demonstrates basic queue message sending using KubeMQTemplate and receiving
 * via the {@code @KubeMQQueueListener} annotation with auto-acknowledgment.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends queue messages</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueueListener} — receives queue messages</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class SendReceiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(SendReceiveApplication.class, args);
    }
}
// Expected output:
// INFO  Sent queue message #1
