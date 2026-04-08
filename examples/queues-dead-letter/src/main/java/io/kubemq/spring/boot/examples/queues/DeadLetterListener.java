package io.kubemq.spring.boot.examples.queues;

import io.kubemq.sdk.queues.QueueMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueueListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterListener {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterListener.class);

    @KubeMQQueueListener(channels = "spring-queues.dead-letter", autoAck = "false")
    public void onMessage(QueueMessageReceived msg) {
        byte[] body = msg.getBody();
        String text = body != null ? new String(body, StandardCharsets.UTF_8) : "<empty>";
        log.info("Received message (rejecting to simulate failure): body={}", text);
        msg.reject();
    }

    @KubeMQQueueListener(channels = "spring-queues.dead-letter.dlq", autoAck = "true")
    public void onDeadLetter(QueueMessageReceived msg) {
        byte[] body = msg.getBody();
        String text = body != null ? new String(body, StandardCharsets.UTF_8) : "<empty>";
        log.info("Dead-letter received: body={}", text);
    }
}

// Expected output:
// INFO  Sent message with DLQ config (max 3 attempts) to spring-queues.dead-letter
// INFO  Dead letter example — reject the message 3 times and it moves to DLQ.
// INFO  Received message (rejecting to simulate failure): body=DLQ test message
// INFO  Received message (rejecting to simulate failure): body=DLQ test message
// INFO  Received message (rejecting to simulate failure): body=DLQ test message
// INFO  Dead-letter received: body=DLQ test message
