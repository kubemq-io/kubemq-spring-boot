package io.kubemq.spring.boot.samples.quickstart;

import io.kubemq.sdk.pubsub.EventMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventListener;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class QuickstartEventListener {

    private static final Logger log = LoggerFactory.getLogger(QuickstartEventListener.class);

    @KubeMQEventListener(channels = "sample.events", group = "quickstart")
    public void onEvent(EventMessageReceived event) {
        String text = new String(event.getBody(), StandardCharsets.UTF_8);
        log.info("Event on {} body={}", event.getChannel(), text);
    }
}
