package io.kubemq.spring.boot.examples.queries;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Handle Query Example
 *
 * <p>Demonstrates the handler side of queries — uses {@code @KubeMQQueryHandler}
 * returning {@code QueryResponseMessage} with a body payload for full response control.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends queries</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueryHandler} — handles queries with full response control</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class HandleQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(HandleQueryApplication.class, args);
    }
}
// Expected output:
// INFO  Query handler received: channel=spring-queries.handle body=Get order status
