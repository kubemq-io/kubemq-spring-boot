package io.kubemq.spring.boot.examples.errorhandling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Reconnection Error Handling Example
 *
 * <p>Demonstrates transparent reconnection with a custom global error handler
 * and connection state monitoring. Stop and restart the KubeMQ broker while
 * this example is running to observe reconnection behaviour.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends events</li>
 *   <li>{@link io.kubemq.sdk.pubsub.PubSubClient} — connection state monitoring</li>
 *   <li>Custom {@link org.springframework.util.ErrorHandler} bean — overrides default error handling</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class ReconnectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReconnectionApplication.class, args);
    }
}
// Expected output:
// INFO  Connection established
