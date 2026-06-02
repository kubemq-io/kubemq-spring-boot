package io.kubemq.spring.boot.samples.eventstore;

import io.kubemq.sdk.pubsub.EventStoreMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventStoreReplayListener {

    private static final Logger log = LoggerFactory.getLogger(EventStoreReplayListener.class);

    @KubeMQEventStoreListener(
            channels = "sample.events_store",
            group = "eventstore-demo",
            subscriptionType = "StartFromFirst")
    public void onReplay(EventStoreMessageReceived event) {
        String text = new String(event.getBody(), StandardCharsets.UTF_8);
        log.info("EventStore seq={} channel={} body={}", event.getSequence(), event.getChannel(), text);
    }
}
