package io.kubemq.spring.boot.examples.eventsstore;

import io.kubemq.sdk.pubsub.EventStoreMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StartFromFirstListener {

    private static final Logger log = LoggerFactory.getLogger(StartFromFirstListener.class);

    @KubeMQEventStoreListener(channels = "spring-events-store.start-from-first", subscriptionType = "StartFromFirst")
    public void onEvent(EventStoreMessageReceived event) {
        String body = (event.getBody() != null ? new String(event.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("Received (from-first): sequence={} body={}", event.getSequence(), body);
    }
}

// Expected output:
// INFO  Received (from-first): sequence=1 body=First-replay event #1
// INFO  Published event #1 to spring-events-store.start-from-first
// ...
// INFO  Start-from-first example completed.
