package io.kubemq.spring.boot.examples.queues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Queue Peek Example
 *
 * <p>Demonstrates peeking at queue messages without consuming them using the
 * programmatic QueuesClient API. Messages are rejected back to the queue
 * after inspection.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends queue messages</li>
 *   <li>{@link io.kubemq.sdk.queues.QueuesClient} — programmatic queue polling</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class PeekApplication {

    public static void main(String[] args) {
        SpringApplication.run(PeekApplication.class, args);
    }
}
// Expected output:
// INFO  Sent message to spring-queues.peek
