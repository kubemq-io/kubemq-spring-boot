package io.kubemq.spring.boot.examples.eventsstore;

import io.kubemq.sdk.pubsub.EventsStoreSubscription;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// This example is structurally similar to the events cancel-subscription example. Kept as separate standalone modules for independent runnability.
@Component
public class EventStoreCancelSubRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EventStoreCancelSubRunner.class);

    private final KubeMQTemplate template;
    private final PubSubClient pubSubClient;

    public EventStoreCancelSubRunner(KubeMQTemplate template, PubSubClient pubSubClient) {
        this.template = template;
        this.pubSubClient = pubSubClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            EventsStoreSubscription subscription = EventsStoreSubscription.builder()
                    .channel("spring-events-store.cancel-sub")
                    .onReceiveEventCallback(event -> {
                        String body = (event.getBody() != null ? new String(event.getBody(), StandardCharsets.UTF_8) : "<empty>");
                        log.info("Received persistent: {}", body);
                    })
                    .onErrorCallback(err -> log.error("Subscription error: {}", err.getMessage()))
                    .build();

            try {
                pubSubClient.subscribeToEventsStore(subscription);
                log.info("Events store subscription started on spring-events-store.cancel-sub");

                Thread.sleep(500);
                template.sendEventStore("spring-events-store.cancel-sub", "Before cancel");
                Thread.sleep(500);
            } finally {
                subscription.cancel();
                log.info("Events store subscription cancelled");
            }

            template.sendEventStore("spring-events-store.cancel-sub", "After cancel (not received)");
            Thread.sleep(500);
            log.info("Events store cancel subscription example completed.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}

// Expected output:
// INFO  Events store subscription started on spring-events-store.cancel-sub
// INFO  Received persistent: Before cancel
// INFO  Events store subscription cancelled
// INFO  Events store cancel subscription example completed.
