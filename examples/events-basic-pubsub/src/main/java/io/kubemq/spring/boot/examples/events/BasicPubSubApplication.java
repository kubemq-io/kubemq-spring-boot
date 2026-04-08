package io.kubemq.spring.boot.examples.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Basic Pub/Sub Example
 *
 * <p>Demonstrates basic event publishing and subscribing using KubeMQTemplate
 * and the {@code @KubeMQEventListener} annotation.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends events</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventListener} — receives events</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class BasicPubSubApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicPubSubApplication.class, args);
    }
}
// Expected output:
// INFO  Received event: channel=spring-events.basic-pubsub body=Hello KubeMQ #1
