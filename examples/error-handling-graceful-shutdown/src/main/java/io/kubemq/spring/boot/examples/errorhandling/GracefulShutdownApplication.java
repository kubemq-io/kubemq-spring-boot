package io.kubemq.spring.boot.examples.errorhandling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Graceful Shutdown Example
 *
 * <p>Demonstrates proper application shutdown using {@code @PreDestroy},
 * {@link org.springframework.context.SmartLifecycle}, and shutdown timeout
 * configuration. In-flight messages are drained before the application stops.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends events</li>
 *   <li>{@link org.springframework.context.SmartLifecycle} — ordered shutdown</li>
 *   <li>{@link jakarta.annotation.PreDestroy} — resource cleanup</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class GracefulShutdownApplication {

    public static void main(String[] args) {
        SpringApplication.run(GracefulShutdownApplication.class, args);
    }
}
// Expected output:
// INFO  SmartLifecycle started
