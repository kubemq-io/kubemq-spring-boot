package io.kubemq.spring.boot.examples.queues;

import io.kubemq.sdk.queues.QueueMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueueListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DelayedListener {

    private static final Logger log = LoggerFactory.getLogger(DelayedListener.class);

    @KubeMQQueueListener(channels = "spring-queues.delayed", autoAck = "true")
    public void onMessage(QueueMessageReceived msg) {
        String body = (msg.getBody() != null ? new String(msg.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("Received delayed message: id={} body={}", msg.getId(), body);
    }
}

// Expected output:
// INFO  Sent delayed message (5s delay) to spring-queues.delayed
// INFO  Delayed queue example — message will be visible to consumers after 5 seconds.
// (after ~5 seconds)
// INFO  Received delayed message: id=<id> body=Delayed message
