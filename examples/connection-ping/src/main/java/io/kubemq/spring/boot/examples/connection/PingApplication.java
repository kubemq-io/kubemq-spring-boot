package io.kubemq.spring.boot.examples.connection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Connection Ping Example
 *
 * <p>Demonstrates verifying connectivity to a KubeMQ broker at startup
 * using the {@link io.kubemq.sdk.pubsub.PubSubClient#ping()} method.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.sdk.pubsub.PubSubClient} — auto-configured by KubeMQ starter</li>
 *   <li>{@link org.springframework.boot.ApplicationRunner} — runs ping at startup</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class PingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PingApplication.class, args);
    }
}
// Expected output:
// INFO  Ping response — host=<host> version=<version> uptime=<seconds>s
