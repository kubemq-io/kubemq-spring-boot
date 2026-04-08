package io.kubemq.spring.boot.examples.eventsstore;

import io.kubemq.sdk.pubsub.EventStoreMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StartNewOnlyListener {

    private static final Logger log = LoggerFactory.getLogger(StartNewOnlyListener.class);

    @KubeMQEventStoreListener(channels = "spring-events-store.start-new-only", subscriptionType = "StartNewOnly")
    public void onEvent(EventStoreMessageReceived event) {
        String body = (event.getBody() != null ? new String(event.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("Received (new-only): sequence={} body={}", event.getSequence(), body);
    }
}

// Expected output:
// INFO  Received (new-only): sequence=1 body=New-only event #1
// INFO  Published event #1 to spring-events-store.start-new-only
// ...
// INFO  Start-new-only example completed.
