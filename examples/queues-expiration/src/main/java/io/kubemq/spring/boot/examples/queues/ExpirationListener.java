package io.kubemq.spring.boot.examples.queues;

import io.kubemq.sdk.queues.QueueMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueueListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExpirationListener {

    private static final Logger log = LoggerFactory.getLogger(ExpirationListener.class);

    @KubeMQQueueListener(channels = "spring-queues.expiration", autoAck = "true")
    public void onMessage(QueueMessageReceived msg) {
        String body = (msg.getBody() != null ? new String(msg.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("Received expiring message: id={} body={}", msg.getId(), body);
    }
}

// Expected output:
// INFO  Sent message with 10s expiration to spring-queues.expiration
// INFO  Expiration example — message will expire if not consumed within 10 seconds.
// INFO  Received expiring message: id=<id> body=Expiring message
