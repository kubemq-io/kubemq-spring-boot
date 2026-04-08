package io.kubemq.spring.boot.examples.events;

import io.kubemq.sdk.pubsub.EventMessageReceived;
import io.kubemq.sdk.pubsub.EventsSubscription;
import io.kubemq.sdk.pubsub.PubSubClient;
import java.nio.charset.StandardCharsets;
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// This example is structurally similar to the events-store cancel-subscription example. Kept as separate standalone modules for independent runnability.
@Component
public class CancelSubscriptionRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CancelSubscriptionRunner.class);

    private final KubeMQTemplate template;
    private final PubSubClient pubSubClient;

    public CancelSubscriptionRunner(KubeMQTemplate template, PubSubClient pubSubClient) {
        this.template = template;
        this.pubSubClient = pubSubClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            EventsSubscription subscription = EventsSubscription.builder()
                    .channel("spring-events.cancel-sub")
                    .onReceiveEventCallback(event -> {
                        String body = (event.getBody() != null ? new String(event.getBody(), StandardCharsets.UTF_8) : "<empty>");
                        log.info("Received: {}", body);
                    })
                    .onErrorCallback(err -> log.error("Subscription error: {}", err.getMessage()))
                    .build();

            try {
                pubSubClient.subscribeToEvents(subscription);
                log.info("Subscription started on spring-events.cancel-sub");

                Thread.sleep(500);
                template.sendEvent("spring-events.cancel-sub", "Before cancel");
                Thread.sleep(500);
            } finally {
                subscription.cancel();
                log.info("Subscription cancelled");
            }

            template.sendEvent("spring-events.cancel-sub", "After cancel (not received)");
            Thread.sleep(500);
            log.info("Cancel subscription example completed.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}

// Expected output:
// INFO  Subscription started on spring-events.cancel-sub
// INFO  Received: Before cancel
// INFO  Subscription cancelled
// INFO  Cancel subscription example completed.
