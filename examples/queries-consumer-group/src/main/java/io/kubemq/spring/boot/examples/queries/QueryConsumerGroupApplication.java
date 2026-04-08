package io.kubemq.spring.boot.examples.queries;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Query Consumer Group Example
 *
 * <p>Demonstrates load-balanced query handling using consumer groups. Two handler
 * beans share the same {@code group} so each query is delivered to only one handler.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends queries</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueryHandler} — load-balanced query handling via consumer groups</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class QueryConsumerGroupApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueryConsumerGroupApplication.class, args);
    }
}
// Expected output:
// INFO  [Handler-A] Query received: Query #1
