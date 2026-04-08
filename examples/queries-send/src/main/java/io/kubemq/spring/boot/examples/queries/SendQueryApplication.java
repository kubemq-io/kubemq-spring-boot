package io.kubemq.spring.boot.examples.queries;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Send Query Example
 *
 * <p>Demonstrates sending a query via KubeMQTemplate and handling it with
 * {@code @KubeMQQueryHandler} returning a {@code QueryResponseMessage} with a body payload.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends queries</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueryHandler} — receives and responds to queries</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class SendQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(SendQueryApplication.class, args);
    }
}
// Expected output:
// INFO  Query received: channel=spring-queries.send body=Get user data
