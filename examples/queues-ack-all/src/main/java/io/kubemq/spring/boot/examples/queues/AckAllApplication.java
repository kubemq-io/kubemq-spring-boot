package io.kubemq.spring.boot.examples.queues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Queue Acknowledge All Example
 *
 * <p>Demonstrates programmatic queue polling and acknowledging all received
 * messages using the QueuesClient API. Messages are sent first, then polled
 * and individually acknowledged.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends queue messages</li>
 *   <li>{@link io.kubemq.sdk.queues.QueuesClient} — programmatic queue polling and ack</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class AckAllApplication {

    public static void main(String[] args) {
        SpringApplication.run(AckAllApplication.class, args);
    }
}
// Expected output:
// INFO  Sent 5 messages to spring-queues.ack-all
