package io.kubemq.spring.boot.samples.queues;

import io.kubemq.sdk.queues.QueueMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueueListener;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class QueueBatchListener {

    private static final Logger log = LoggerFactory.getLogger(QueueBatchListener.class);

    @KubeMQQueueListener(
            channels = "sample.queue",
            batch = "true",
            maxPollMessages = "10",
            autoAck = "true",
            pollTimeout = "5")
    public void onBatch(List<QueueMessageReceived> messages) {
        for (QueueMessageReceived msg : messages) {
            String text = new String(msg.getBody(), StandardCharsets.UTF_8);
            log.info("Queue batch item id={} body={}", msg.getId(), text);
        }
    }
}
