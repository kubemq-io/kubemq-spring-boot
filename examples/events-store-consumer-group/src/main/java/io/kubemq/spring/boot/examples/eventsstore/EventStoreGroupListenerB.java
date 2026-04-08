package io.kubemq.spring.boot.examples.eventsstore;

import io.kubemq.sdk.pubsub.EventStoreMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventStoreGroupListenerB {

    private static final Logger log = LoggerFactory.getLogger(EventStoreGroupListenerB.class);

    @KubeMQEventStoreListener(
            channels = "spring-events-store.consumer-group",
            subscriptionType = "StartNewOnly",
            group = "store-group")
    public void onEvent(EventStoreMessageReceived event) {
        String body = (event.getBody() != null ? new String(event.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("[Store-Listener-B] Received: sequence={} body={}", event.getSequence(), body);
    }
}

// Expected output:
// INFO  Sent persistent event #1 to spring-events-store.consumer-group
// INFO  [Store-Listener-A] Received: sequence=1 body=Persistent msg #1
// INFO  Sent persistent event #2 to spring-events-store.consumer-group
// INFO  [Store-Listener-B] Received: sequence=2 body=Persistent msg #2
// ...
// INFO  Events store consumer group example completed.
