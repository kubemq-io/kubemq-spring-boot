package io.kubemq.spring.boot.examples.queues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Queue Message Expiration Example
 *
 * <p>Demonstrates sending a queue message with a TTL (time-to-live) using the
 * fluent KubeMQQueueMessageBuilder. If the message is not consumed within
 * the expiration period, it is automatically removed from the queue.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — fluent queue message builder</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueueListener} — receives expiring messages</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class ExpirationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpirationApplication.class, args);
    }
}
// Expected output:
// INFO  Sent message with 10s expiration to spring-queues.expiration
