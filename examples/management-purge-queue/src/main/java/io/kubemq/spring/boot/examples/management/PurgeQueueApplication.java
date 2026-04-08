package io.kubemq.spring.boot.examples.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Purge Queue Example
 *
 * <p>Demonstrates sending messages to a queue channel and then purging
 * all pending messages using the QueuesClient API directly.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends queue messages</li>
 *   <li>{@link io.kubemq.sdk.queues.QueuesClient} — purges the queue channel</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class PurgeQueueApplication {

    public static void main(String[] args) {
        SpringApplication.run(PurgeQueueApplication.class, args);
    }
}
// Expected output:
// INFO  Sent 5 messages to spring-mgmt.purge-queue
