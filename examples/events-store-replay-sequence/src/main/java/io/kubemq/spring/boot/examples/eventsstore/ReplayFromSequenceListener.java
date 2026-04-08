package io.kubemq.spring.boot.examples.eventsstore;

import io.kubemq.sdk.pubsub.EventStoreMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReplayFromSequenceListener {

    private static final Logger log = LoggerFactory.getLogger(ReplayFromSequenceListener.class);

    @KubeMQEventStoreListener(
            channels = "spring-events-store.replay-sequence",
            subscriptionType = "StartAtSequence",
            subscriptionValue = "5")
    public void onEvent(EventStoreMessageReceived event) {
        String body = (event.getBody() != null ? new String(event.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("Replayed from sequence: sequence={} body={}", event.getSequence(), body);
    }
}

// Expected output:
// INFO  Replayed from sequence: sequence=5 body=Sequenced event #5
// INFO  Replayed from sequence: sequence=6 body=Sequenced event #6
// ...
// INFO  Replay-from-sequence example completed.
