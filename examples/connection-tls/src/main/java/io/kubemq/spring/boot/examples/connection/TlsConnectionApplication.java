package io.kubemq.spring.boot.examples.connection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TLS Connection Example
 *
 * <p>Demonstrates connecting to a KubeMQ broker with TLS encryption.
 * Configure certificate paths via environment variables or application.yml.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>KubeMQ auto-configuration with TLS properties</li>
 *   <li>{@link io.kubemq.sdk.pubsub.PubSubClient} — verifies connection with ping</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running with TLS enabled</li>
 *   <li>Valid client certificate and CA certificate files</li>
 * </ul>
 */
@SpringBootApplication
public class TlsConnectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(TlsConnectionApplication.class, args);
    }
}
// Expected output:
// INFO  TLS connection successful — host=<host> version=<version>
