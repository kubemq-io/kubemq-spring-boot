package io.kubemq.spring.boot.examples.connection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Auth Token Connection Example
 *
 * <p>Demonstrates connecting to a KubeMQ broker using an authentication token.
 * Set the token via the {@code KUBEMQ_AUTH_TOKEN} environment variable or in application.yml.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>KubeMQ auto-configuration with auth-token property</li>
 *   <li>{@link io.kubemq.sdk.pubsub.PubSubClient} — verifies connection with ping</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running with authentication enabled</li>
 *   <li>Valid authentication token</li>
 * </ul>
 */
@SpringBootApplication
public class AuthTokenConnectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthTokenConnectionApplication.class, args);
    }
}
// Expected output:
// INFO  Auth-token connection successful — host=<host> version=<version>
