package io.kubemq.spring.boot.examples.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Template Fluent Builders Example
 *
 * <p>Demonstrates all fluent builder APIs on {@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate}
 * for events, queues, commands, and queries.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — fluent builder API</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQCommandHandler} — command handling</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueryHandler} — query handling</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class TemplateFluentBuildersApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplateFluentBuildersApplication.class, args);
    }
}
// Expected output:
// INFO  Sent event via fluent builder with tag
