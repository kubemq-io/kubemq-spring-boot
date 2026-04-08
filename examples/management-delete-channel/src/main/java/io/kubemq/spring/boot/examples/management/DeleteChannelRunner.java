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
public class DeleteChannelRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DeleteChannelRunner.class);

    private final PubSubClient pubSubClient;
    private final CQClient cqClient;
    private final QueuesClient queuesClient;

    public DeleteChannelRunner(PubSubClient pubSubClient, CQClient cqClient, QueuesClient queuesClient) {
        this.pubSubClient = pubSubClient;
        this.cqClient = cqClient;
        this.queuesClient = queuesClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            pubSubClient.createEventsChannel("spring-mgmt.delete-test");
            log.info("Created events channel for deletion test");

            boolean deleted = pubSubClient.deleteEventsChannel("spring-mgmt.delete-test");
            log.info("Deleted events channel: spring-mgmt.delete-test — success={}", deleted);

            cqClient.createCommandsChannel("spring-mgmt.delete-cmd-test");
            boolean cmdDeleted = cqClient.deleteCommandsChannel("spring-mgmt.delete-cmd-test");
            log.info("Deleted commands channel: spring-mgmt.delete-cmd-test — success={}", cmdDeleted);

            log.info("Delete channel example completed.");
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}

// Expected output:
// INFO  Created events channel for deletion test
// INFO  Deleted events channel: spring-mgmt.delete-test — success=true
// INFO  Deleted commands channel: spring-mgmt.delete-cmd-test — success=true
// INFO  Delete channel example completed.
