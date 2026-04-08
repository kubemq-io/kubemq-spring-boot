package io.kubemq.spring.boot.examples.management;

import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CreateChannelRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CreateChannelRunner.class);

    private final PubSubClient pubSubClient;
    private final CQClient cqClient;
    private final QueuesClient queuesClient;

    public CreateChannelRunner(PubSubClient pubSubClient, CQClient cqClient, QueuesClient queuesClient) {
        this.pubSubClient = pubSubClient;
        this.cqClient = cqClient;
        this.queuesClient = queuesClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            pubSubClient.createEventsChannel("spring-mgmt.events-channel");
            log.info("Created events channel: spring-mgmt.events-channel");

            pubSubClient.createEventsStoreChannel("spring-mgmt.events-store-channel");
            log.info("Created events store channel: spring-mgmt.events-store-channel");

            cqClient.createCommandsChannel("spring-mgmt.commands-channel");
            log.info("Created commands channel: spring-mgmt.commands-channel");

            cqClient.createQueriesChannel("spring-mgmt.queries-channel");
            log.info("Created queries channel: spring-mgmt.queries-channel");

            queuesClient.createQueuesChannel("spring-mgmt.queues-channel");
            log.info("Created queues channel: spring-mgmt.queues-channel");

            log.info("Create channel example completed.");
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}

// Expected output:
// INFO  Created events channel: spring-mgmt.events-channel
// INFO  Created events store channel: spring-mgmt.events-store-channel
// INFO  Created commands channel: spring-mgmt.commands-channel
// INFO  Created queries channel: spring-mgmt.queries-channel
// INFO  Created queues channel: spring-mgmt.queues-channel
// INFO  Create channel example completed.
