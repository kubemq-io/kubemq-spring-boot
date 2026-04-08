package io.kubemq.spring.boot.examples.queues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Queue Delayed Delivery Example
 *
 * <p>Demonstrates sending a queue message with a delay using the fluent
 * KubeMQQueueMessageBuilder. The message becomes visible to consumers
 * only after the specified delay period.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — fluent queue message builder</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueueListener} — receives delayed messages</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class DelayedApplication {

    public static void main(String[] args) {
        SpringApplication.run(DelayedApplication.class, args);
    }
}
// Expected output:
// INFO  Sent delayed message (5s delay) to spring-queues.delayed
