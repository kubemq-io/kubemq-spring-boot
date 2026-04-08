package io.kubemq.spring.boot.examples.eventsstore;

import io.kubemq.sdk.pubsub.EventStoreMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BasicEventStoreListener {

    private static final Logger log = LoggerFactory.getLogger(BasicEventStoreListener.class);

    @KubeMQEventStoreListener(channels = "spring-events-store.basic", subscriptionType = "StartNewOnly")
    public void onEvent(EventStoreMessageReceived event) {
        String body = (event.getBody() != null ? new String(event.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("Received persistent event: channel={} sequence={} body={}",
                event.getChannel(), event.getSequence(), body);
    }
}

// Expected output:
// INFO  Received persistent event: channel=spring-events-store.basic sequence=1 body=Persistent event #1
// INFO  Published persistent event #1 to spring-events-store.basic
// ...
// INFO  Basic event store example completed.
