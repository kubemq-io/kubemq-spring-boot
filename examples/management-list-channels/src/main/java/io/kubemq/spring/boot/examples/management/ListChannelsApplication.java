package io.kubemq.spring.boot.examples.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * List Channels Example
 *
 * <p>Demonstrates listing channels of all types (events, events store,
 * commands, queries, queues) filtered by a search prefix using the
 * KubeMQ SDK clients directly.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.sdk.pubsub.PubSubClient} — lists events and events store channels</li>
 *   <li>{@link io.kubemq.sdk.cq.CQClient} — lists commands and queries channels</li>
 *   <li>{@link io.kubemq.sdk.queues.QueuesClient} — lists queue channels</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class ListChannelsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ListChannelsApplication.class, args);
    }
}
// Expected output:
// INFO  Events channels matching 'spring-mgmt': <count>
