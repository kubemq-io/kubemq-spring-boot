package io.kubemq.spring.boot.examples.events;

import io.kubemq.sdk.pubsub.EventMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Paired with ConsumerGroupListenerA to demonstrate consumer group load balancing. Both listeners are identical except for the log prefix.
@Component
public class ConsumerGroupListenerB {

    private static final Logger log = LoggerFactory.getLogger(ConsumerGroupListenerB.class);

    @KubeMQEventListener(channels = "spring-events.consumer-group", group = "my-group")
    public void onEvent(EventMessageReceived event) {
        String body = (event.getBody() != null ? new String(event.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("[Listener-B] Received: {}", body);
    }
}

// Expected output:
// INFO  Sent event #1 to spring-events.consumer-group
// INFO  [Listener-A] Received: Message #1
// INFO  Sent event #2 to spring-events.consumer-group
// INFO  [Listener-B] Received: Message #2
// INFO  Sent event #3 to spring-events.consumer-group
// INFO  [Listener-A] Received: Message #3
// ...
// INFO  Consumer group example completed.
