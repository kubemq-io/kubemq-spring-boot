package io.kubemq.spring.boot.examples.queues;

import io.kubemq.sdk.queues.QueueMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueueListener;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BatchSendListener {

    private static final Logger log = LoggerFactory.getLogger(BatchSendListener.class);

    @KubeMQQueueListener(channels = "spring-queues.batch-send", batch = "true", maxPollMessages = "10", autoAck = "true")
    public void onMessages(List<QueueMessageReceived> messages) {
        log.info("Received batch of {} messages", messages.size());
        for (QueueMessageReceived msg : messages) {
            String body = (msg.getBody() != null ? new String(msg.getBody(), StandardCharsets.UTF_8) : "<empty>");
            log.info("  Batch message: id={} body={}", msg.getId(), body);
        }
    }
}

// Expected output:
// INFO  Sent 3 messages in batch to spring-queues.batch-send
// INFO  Batch send example — messages sent, listener will process them.
// INFO  Received batch of 3 messages
// INFO    Batch message: id=<id> body=Batch msg #1
// INFO    Batch message: id=<id> body=Batch msg #2
// INFO    Batch message: id=<id> body=Batch msg #3
