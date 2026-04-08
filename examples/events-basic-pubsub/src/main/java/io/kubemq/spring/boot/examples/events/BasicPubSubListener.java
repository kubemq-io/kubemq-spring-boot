package io.kubemq.spring.boot.examples.events;

import io.kubemq.sdk.pubsub.EventMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BasicPubSubListener {

    private static final Logger log = LoggerFactory.getLogger(BasicPubSubListener.class);

    @KubeMQEventListener(channels = "spring-events.basic-pubsub")
    public void onEvent(EventMessageReceived event) {
        byte[] body = event.getBody();
        String text = body != null ? new String(body, StandardCharsets.UTF_8) : "<empty>";
        log.info("Received event: channel={} body={}", event.getChannel(), text);
    }
}

// Expected output:
// INFO  Received event: channel=spring-events.basic-pubsub body=Hello KubeMQ #1
// INFO  Published event to spring-events.basic-pubsub: Hello KubeMQ #1
// INFO  Received event: channel=spring-events.basic-pubsub body=Hello KubeMQ #2
// INFO  Published event to spring-events.basic-pubsub: Hello KubeMQ #2
// INFO  Received event: channel=spring-events.basic-pubsub body=Hello KubeMQ #3
// INFO  Published event to spring-events.basic-pubsub: Hello KubeMQ #3
// INFO  Basic pub/sub example completed.
