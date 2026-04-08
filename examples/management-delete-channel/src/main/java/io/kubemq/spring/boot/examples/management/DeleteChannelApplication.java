package io.kubemq.spring.boot.examples.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Delete Channel Example
 *
 * <p>Demonstrates creating and then deleting channels using the KubeMQ SDK
 * clients directly. Creates temporary channels, then removes them.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.sdk.pubsub.PubSubClient} — creates and deletes events channels</li>
 *   <li>{@link io.kubemq.sdk.cq.CQClient} — creates and deletes commands channels</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class DeleteChannelApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeleteChannelApplication.class, args);
    }
}
// Expected output:
// INFO  Created events channel for deletion test
