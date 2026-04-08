package io.kubemq.spring.boot.examples.events;

import io.kubemq.sdk.pubsub.EventMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WildcardSubscribeListener {

    private static final Logger log = LoggerFactory.getLogger(WildcardSubscribeListener.class);

    @KubeMQEventListener(channels = "spring-events.orders.>")
    public void onEvent(EventMessageReceived event) {
        byte[] body = event.getBody();
        String text = body != null ? new String(body, StandardCharsets.UTF_8) : "<empty>";
        log.info("Wildcard received: channel={} body={}", event.getChannel(), text);
    }
}

// Expected output:
// INFO  Sent event to spring-events.orders.us
// INFO  Wildcard received: channel=spring-events.orders.us body=US order
// INFO  Sent event to spring-events.orders.eu
// INFO  Wildcard received: channel=spring-events.orders.eu body=EU order
// INFO  Sent event to spring-events.orders.asia
// INFO  Wildcard received: channel=spring-events.orders.asia body=Asia order
// INFO  Wildcard subscribe example completed.
