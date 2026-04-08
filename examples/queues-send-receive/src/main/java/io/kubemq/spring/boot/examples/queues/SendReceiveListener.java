package io.kubemq.spring.boot.examples.queues;

import io.kubemq.sdk.queues.QueueMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueueListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SendReceiveListener {

    private static final Logger log = LoggerFactory.getLogger(SendReceiveListener.class);

    @KubeMQQueueListener(channels = "spring-queues.send-receive", autoAck = "true")
    public void onMessage(QueueMessageReceived msg) {
        byte[] body = msg.getBody();
        String text = body != null ? new String(body, StandardCharsets.UTF_8) : "<empty>";
        log.info("Received queue message: id={} body={}", msg.getId(), text);
    }
}

// Expected output:
// INFO  Sent queue message #1
// INFO  Sent queue message #2
// INFO  Sent queue message #3
// INFO  Send/receive example — messages sent, listener will process them.
// INFO  Received queue message: id=<id> body=Queue message #1
// INFO  Received queue message: id=<id> body=Queue message #2
// INFO  Received queue message: id=<id> body=Queue message #3
