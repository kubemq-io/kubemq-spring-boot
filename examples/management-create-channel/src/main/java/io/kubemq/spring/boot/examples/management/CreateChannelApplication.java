package io.kubemq.spring.boot.examples.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Create Channel Example
 *
 * <p>Demonstrates creating channels of all types (events, events store,
 * commands, queries, queues) using the KubeMQ SDK clients directly.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.sdk.pubsub.PubSubClient} — creates events and events store channels</li>
 *   <li>{@link io.kubemq.sdk.cq.CQClient} — creates commands and queries channels</li>
 *   <li>{@link io.kubemq.sdk.queues.QueuesClient} — creates queue channels</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class CreateChannelApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreateChannelApplication.class, args);
    }
}
// Expected output:
// INFO  Created events channel: spring-mgmt.events-channel
