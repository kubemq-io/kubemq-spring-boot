package io.kubemq.spring.boot.examples.eventsstore;

import io.kubemq.sdk.pubsub.EventStoreMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StartFromLastListener {

    private static final Logger log = LoggerFactory.getLogger(StartFromLastListener.class);

    @KubeMQEventStoreListener(channels = "spring-events-store.start-from-last", subscriptionType = "StartFromLast")
    public void onEvent(EventStoreMessageReceived event) {
        String body = (event.getBody() != null ? new String(event.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("Received (from-last): sequence={} body={}", event.getSequence(), body);
    }
}

// Expected output:
// INFO  Received (from-last): sequence=5 body=Last-replay event #5
// INFO  Published event #5 to spring-events-store.start-from-last
// INFO  Start-from-last example completed.
