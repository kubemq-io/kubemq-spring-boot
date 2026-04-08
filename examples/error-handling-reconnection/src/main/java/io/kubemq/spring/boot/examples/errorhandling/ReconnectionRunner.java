package io.kubemq.spring.boot.examples.errorhandling;

import io.kubemq.sdk.client.ConnectionStateListener;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ReconnectionRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ReconnectionRunner.class);

    private final KubeMQTemplate template;
    private final PubSubClient pubSubClient;

    public ReconnectionRunner(KubeMQTemplate template, PubSubClient pubSubClient) {
        this.template = template;
        this.pubSubClient = pubSubClient;
    }

    @PostConstruct
    public void registerConnectionStateListener() {
        pubSubClient.addConnectionStateListener(new ConnectionStateListener() {
            @Override
            public void onConnected() {
                log.info("Connection established");
            }

            @Override
            public void onDisconnected() {
                log.warn("Connection lost — reconnecting automatically");
            }

            @Override
            public void onReconnecting(int attempt) {
                log.info("Reconnection attempt #{}", attempt);
            }

            @Override
            public void onReconnected() {
                log.info("Reconnection successful — subscriptions recovered");
            }

            @Override
            public void onClosed() {
                log.info("Connection closed");
            }
        });
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            template.sendEvent("spring-error.reconnect", "Test event");
            log.info("Sent event — stop/restart the broker to see reconnection");
            log.info("Reconnection example started (Ctrl+C to exit).");
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}

// Expected output:
// INFO  Connection established
// INFO  Sent event — stop/restart the broker to see reconnection
// INFO  Reconnection example started (Ctrl+C to exit).
// (stop broker)
// WARN  Connection lost — reconnecting automatically
// INFO  Reconnection attempt #1
// (restart broker)
// INFO  Reconnection successful — subscriptions recovered
