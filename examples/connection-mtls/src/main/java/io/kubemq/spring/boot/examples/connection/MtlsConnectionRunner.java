package io.kubemq.spring.boot.examples.connection;

import io.kubemq.sdk.common.ServerInfo;
import io.kubemq.sdk.pubsub.PubSubClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MtlsConnectionRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MtlsConnectionRunner.class);

    private final PubSubClient pubSubClient;

    public MtlsConnectionRunner(PubSubClient pubSubClient) {
        this.pubSubClient = pubSubClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            ServerInfo info = pubSubClient.ping();
            log.info("mTLS connection successful — host={} version={}", info.getHost(), info.getVersion());
            log.info("mTLS connection example completed.");
        } catch (Exception ex) {
            log.warn("mTLS connection failed (check cert/key paths): {}", ex);
        }
    }
}

// Expected output:
// INFO  mTLS connection successful — host=<host> version=<version>
// INFO  mTLS connection example completed.
