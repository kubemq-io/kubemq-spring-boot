package io.kubemq.spring.boot.examples.queues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Queue Batch Send Example
 *
 * <p>Demonstrates sending multiple queue messages in a single batch using
 * KubeMQTemplate and receiving them as a batch via {@code @KubeMQQueueListener}
 * with batch mode enabled.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends batch queue messages</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueueListener} — receives batch queue messages</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class BatchSendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchSendApplication.class, args);
    }
}
// Expected output:
// INFO  Sent 3 messages in batch to spring-queues.batch-send
