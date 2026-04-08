package io.kubemq.spring.boot.examples.scs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Cloud Stream Queues Example
 *
 * <p>Demonstrates using Spring Cloud Stream with KubeMQ binder for queue-based messaging.
 * Messages flow through a {@code Function<String, String>} bean bound to KubeMQ queue channels.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>Spring Cloud Stream with KubeMQ binder (QUEUES pattern)</li>
 *   <li>{@link org.springframework.cloud.stream.function.StreamBridge} — programmatic message sending</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class ScsQueuesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScsQueuesApplication.class, args);
    }
}
// Expected output:
// INFO  StreamBridge send (queues) result: true
