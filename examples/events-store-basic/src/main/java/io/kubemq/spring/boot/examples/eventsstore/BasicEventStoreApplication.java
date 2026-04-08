package io.kubemq.spring.boot.examples.eventsstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Basic Event Store Example
 *
 * <p>Demonstrates basic persistent event publishing and subscribing using KubeMQTemplate
 * and the {@code @KubeMQEventStoreListener} annotation with {@code StartNewOnly} subscription type.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends persistent events</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener} — receives persistent events</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class BasicEventStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicEventStoreApplication.class, args);
    }
}
// Expected output:
// INFO  Received persistent event: channel=spring-events-store.basic sequence=1 body=Persistent event #1
