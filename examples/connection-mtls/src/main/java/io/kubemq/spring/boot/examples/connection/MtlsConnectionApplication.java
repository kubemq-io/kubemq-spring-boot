package io.kubemq.spring.boot.examples.connection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Mutual TLS (mTLS) Connection Example
 *
 * <p>Demonstrates connecting to a KubeMQ broker with mutual TLS authentication.
 * Both client and server verify each other's certificates.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>KubeMQ auto-configuration with mTLS properties (cert, key, CA)</li>
 *   <li>{@link io.kubemq.sdk.pubsub.PubSubClient} — verifies connection with ping</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running with mTLS enabled</li>
 *   <li>Valid client certificate, client key, and CA certificate files</li>
 * </ul>
 */
@SpringBootApplication
public class MtlsConnectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MtlsConnectionApplication.class, args);
    }
}
// Expected output:
// INFO  mTLS connection successful — host=<host> version=<version>
