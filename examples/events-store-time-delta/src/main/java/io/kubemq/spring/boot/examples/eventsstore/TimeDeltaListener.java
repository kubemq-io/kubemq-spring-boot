package io.kubemq.spring.boot.examples.eventsstore;

import io.kubemq.sdk.pubsub.EventStoreMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TimeDeltaListener {

    private static final Logger log = LoggerFactory.getLogger(TimeDeltaListener.class);

    @KubeMQEventStoreListener(
            channels = "spring-events-store.time-delta",
            subscriptionType = "StartAtTimeDelta",
            subscriptionValue = "60")
    public void onEvent(EventStoreMessageReceived event) {
        String body = (event.getBody() != null ? new String(event.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("Received (time-delta 60s): sequence={} body={}", event.getSequence(), body);
    }
}

// Expected output:
// INFO  Received (time-delta 60s): sequence=1 body=Delta event #1
// ...
// INFO  Time-delta example completed.
