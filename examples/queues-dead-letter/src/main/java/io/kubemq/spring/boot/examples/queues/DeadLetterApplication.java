package io.kubemq.spring.boot.examples.queues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Queue Dead Letter Example
 *
 * <p>Demonstrates dead-letter queue routing. Messages that are rejected beyond
 * the maximum receive count are automatically moved to a designated dead-letter
 * queue channel.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — fluent queue message builder with DLQ config</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueueListener} — receives and rejects messages</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class DeadLetterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeadLetterApplication.class, args);
    }
}
// Expected output:
// INFO  Sent message with DLQ config (max 3 attempts) to spring-queues.dead-letter
