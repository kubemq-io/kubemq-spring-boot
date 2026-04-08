package io.kubemq.spring.boot.examples.queues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Queue Ack/Reject Example
 *
 * <p>Demonstrates conditional acknowledgment and rejection of queue messages.
 * Valid messages are acknowledged while invalid messages are rejected using
 * manual ack mode.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends queue messages</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueueListener} — manual ack/reject</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class AckRejectApplication {

    public static void main(String[] args) {
        SpringApplication.run(AckRejectApplication.class, args);
    }
}
// Expected output:
// INFO  Sent 3 messages (1 invalid) to spring-queues.ack-reject
