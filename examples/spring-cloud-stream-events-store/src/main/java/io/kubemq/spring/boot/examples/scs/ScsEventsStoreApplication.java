package io.kubemq.spring.boot.examples.scs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Cloud Stream Events Store Example
 *
 * <p>Demonstrates using Spring Cloud Stream with KubeMQ binder for persistent event processing
 * (events-store pattern). Messages are persisted and replayed on reconnection.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>Spring Cloud Stream with KubeMQ binder (EVENTS_STORE pattern)</li>
 *   <li>{@link org.springframework.cloud.stream.function.StreamBridge} — programmatic message sending</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class ScsEventsStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScsEventsStoreApplication.class, args);
    }
}
// Expected output:
// INFO  StreamBridge send (events-store) result: true
