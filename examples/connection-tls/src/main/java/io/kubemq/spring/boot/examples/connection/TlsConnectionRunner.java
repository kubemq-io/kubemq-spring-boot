package io.kubemq.spring.boot.examples.connection;

import io.kubemq.sdk.common.ServerInfo;
import io.kubemq.sdk.pubsub.PubSubClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TlsConnectionRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TlsConnectionRunner.class);

    private final PubSubClient pubSubClient;

    public TlsConnectionRunner(PubSubClient pubSubClient) {
        this.pubSubClient = pubSubClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            ServerInfo info = pubSubClient.ping();
            log.info("TLS connection successful — host={} version={}", info.getHost(), info.getVersion());
            log.info("TLS connection example completed.");
        } catch (Exception ex) {
            log.warn("TLS connection failed (check cert paths): {}", ex);
        }
    }
}

// Expected output:
// INFO  TLS connection successful — host=<host> version=<version>
// INFO  TLS connection example completed.
