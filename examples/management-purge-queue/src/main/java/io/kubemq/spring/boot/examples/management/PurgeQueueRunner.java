package io.kubemq.spring.boot.examples.management;

import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PurgeQueueRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PurgeQueueRunner.class);

    private final KubeMQTemplate template;
    private final QueuesClient queuesClient;

    public PurgeQueueRunner(KubeMQTemplate template, QueuesClient queuesClient) {
        this.template = template;
        this.queuesClient = queuesClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            for (int i = 1; i <= 5; i++) {
                template.sendQueueMessage("spring-mgmt.purge-queue", "Purge msg #" + i);
            }
            log.info("Sent 5 messages to spring-mgmt.purge-queue");

            queuesClient.purgeQueue("spring-mgmt.purge-queue");
            log.info("Purged all messages from spring-mgmt.purge-queue");

            log.info("Purge queue example completed.");
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}

// Expected output:
// INFO  Sent 5 messages to spring-mgmt.purge-queue
// INFO  Purged all messages from spring-mgmt.purge-queue
// INFO  Purge queue example completed.
