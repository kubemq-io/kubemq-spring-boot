package io.kubemq.spring.boot.examples.queries;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Cached Query Example
 *
 * <p>Demonstrates cached query responses using the fluent builder with
 * {@code withCacheKey} and {@code withCacheTTL}. The first query hits the handler;
 * subsequent queries with the same cache key return the cached response.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends cached queries via fluent builder</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueryHandler} — handles the initial query</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class CachedQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(CachedQueryApplication.class, args);
    }
}
// Expected output:
// INFO  Cache handler invoked (first call only): body=Get config
