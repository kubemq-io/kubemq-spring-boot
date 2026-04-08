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
public class PeekRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PeekRunner.class);

    private final KubeMQTemplate template;
    private final QueuesClient queuesClient;

    public PeekRunner(KubeMQTemplate template, QueuesClient queuesClient) {
        this.template = template;
        this.queuesClient = queuesClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            template.sendQueueMessage("spring-queues.peek", "Peek me");
            log.info("Sent message to spring-queues.peek");

            QueuesPollRequest peekRequest = QueuesPollRequest.builder()
                    .channel("spring-queues.peek")
                    .pollMaxMessages(1)
                    .pollWaitTimeoutInSeconds(5)
                    .autoAckMessages(false)
                    .build();
            QueuesPollResponse response = queuesClient.receiveQueuesMessages(peekRequest);
            if (response.getMessages() != null && !response.getMessages().isEmpty()) {
                QueueMessageReceived msg = response.getMessages().get(0);
                String body = (msg.getBody() != null ? new String(msg.getBody(), StandardCharsets.UTF_8) : "<empty>");
                log.info("Peeked message: id={} body={}", msg.getId(), body);
                msg.reject();
                log.info("Rejected message (returned to queue for later consumption)");
            }
            log.info("Peek example completed.");
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}

// Expected output:
// INFO  Sent message to spring-queues.peek
// INFO  Peeked message: id=<id> body=Peek me
// INFO  Rejected message (returned to queue for later consumption)
// INFO  Peek example completed.
