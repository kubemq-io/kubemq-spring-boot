package io.kubemq.spring.boot.examples.events;

import io.kubemq.sdk.pubsub.EventMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Paired with SubscriberTwo to demonstrate multiple subscribers on the same channel. Both listeners are identical except for the log prefix.
@Component
public class SubscriberOne {

    private static final Logger log = LoggerFactory.getLogger(SubscriberOne.class);

    @KubeMQEventListener(channels = "spring-events.multi-sub")
    public void onEvent(EventMessageReceived event) {
        String body = (event.getBody() != null ? new String(event.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("[Subscriber-1] Received: {}", body);
    }
}
