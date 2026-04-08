package io.kubemq.spring.boot.examples.queues;

import io.kubemq.sdk.queues.QueueMessageReceived;
import io.kubemq.sdk.queues.QueuesPollRequest;
import io.kubemq.sdk.queues.QueuesPollResponse;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AckAllRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AckAllRunner.class);

    private final KubeMQTemplate template;
    private final QueuesClient queuesClient;

    public AckAllRunner(KubeMQTemplate template, QueuesClient queuesClient) {
        this.template = template;
        this.queuesClient = queuesClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            for (int i = 1; i <= 5; i++) {
                template.sendQueueMessage("spring-queues.ack-all", "Ack-all msg #" + i);
            }
            log.info("Sent 5 messages to spring-queues.ack-all");

            QueuesPollRequest request = QueuesPollRequest.builder()
                    .channel("spring-queues.ack-all")
                    .pollMaxMessages(10)
                    .pollWaitTimeoutInSeconds(5)
                    .autoAckMessages(false)
                    .build();
            QueuesPollResponse response = queuesClient.receiveQueuesMessages(request);
            if (response.getMessages() != null) {
                for (QueueMessageReceived msg : response.getMessages()) {
                    String body = (msg.getBody() != null ? new String(msg.getBody(), StandardCharsets.UTF_8) : "<empty>");
                    log.info("Received: {}", body);
                    try {
                        msg.ack();
                    } catch (Exception ackEx) {
                        log.warn("Failed to ack message body={}: {}", body, ackEx.getMessage());
                    }
                }
                log.info("Acknowledged all {} messages", response.getMessages().size());
            }
            log.info("Ack-all example completed.");
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}

// Expected output:
// INFO  Sent 5 messages to spring-queues.ack-all
// INFO  Received: Ack-all msg #1
// INFO  Received: Ack-all msg #2
// ...
// INFO  Acknowledged all 5 messages
// INFO  Ack-all example completed.
