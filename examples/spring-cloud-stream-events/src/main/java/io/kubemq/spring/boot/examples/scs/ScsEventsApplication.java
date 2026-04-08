package io.kubemq.spring.boot.examples.scs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Cloud Stream Events Example
 *
 * <p>Demonstrates using Spring Cloud Stream with KubeMQ binder for event processing.
 * Messages flow through a {@code Function<String, String>} bean bound to KubeMQ channels.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>Spring Cloud Stream with KubeMQ binder</li>
 *   <li>{@link org.springframework.cloud.stream.function.StreamBridge} — programmatic message sending</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class ScsEventsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScsEventsApplication.class, args);
    }
}
// Expected output:
// INFO  StreamBridge send result: true
