package io.kubemq.spring.boot.examples.queues;

import io.kubemq.sdk.queues.QueueMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueueListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AckRejectListener {

    private static final Logger log = LoggerFactory.getLogger(AckRejectListener.class);

    @KubeMQQueueListener(channels = "spring-queues.ack-reject", autoAck = "false")
    public void onMessage(QueueMessageReceived msg) {
        byte[] body = msg.getBody();
        String text = body != null ? new String(body, StandardCharsets.UTF_8) : "<empty>";
        if ("INVALID".equals(text)) {
            log.warn("Rejecting invalid message: id={}", msg.getId());
            msg.reject();
        } else {
            log.info("Processing and acknowledging: id={} body={}", msg.getId(), text);
            msg.ack();
        }
    }
}

// Expected output:
// INFO  Sent 3 messages (1 invalid) to spring-queues.ack-reject
// INFO  Processing and acknowledging: id=<id> body=Valid order
// WARN  Rejecting invalid message: id=<id>
// INFO  Processing and acknowledging: id=<id> body=Another valid order
