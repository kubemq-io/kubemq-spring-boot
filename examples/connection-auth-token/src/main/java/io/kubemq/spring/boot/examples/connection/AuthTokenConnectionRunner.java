package io.kubemq.spring.boot.examples.connection;

import io.kubemq.sdk.common.ServerInfo;
import io.kubemq.sdk.pubsub.PubSubClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenConnectionRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AuthTokenConnectionRunner.class);

    private final PubSubClient pubSubClient;

    public AuthTokenConnectionRunner(PubSubClient pubSubClient) {
        this.pubSubClient = pubSubClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            ServerInfo info = pubSubClient.ping();
            log.info("Auth-token connection successful — host={} version={}", info.getHost(), info.getVersion());
            log.info("Auth-token connection example completed.");
        } catch (Exception ex) {
            log.warn("Auth-token connection failed (check token): {}", ex);
        }
    }
}

// Expected output:
// INFO  Auth-token connection successful — host=<host> version=<version>
// INFO  Auth-token connection example completed.
