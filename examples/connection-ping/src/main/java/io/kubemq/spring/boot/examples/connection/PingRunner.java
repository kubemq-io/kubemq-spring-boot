package io.kubemq.spring.boot.examples.connection;

import io.kubemq.sdk.common.ServerInfo;
import io.kubemq.sdk.pubsub.PubSubClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PingRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PingRunner.class);

    private final PubSubClient pubSubClient;

    public PingRunner(PubSubClient pubSubClient) {
        this.pubSubClient = pubSubClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            ServerInfo info = pubSubClient.ping();
            log.info("Ping response — host={} version={} uptime={}s",
                    info.getHost(), info.getVersion(), info.getServerUpTimeSeconds());
            log.info("Connection ping example completed.");
        } catch (Exception ex) {
            log.warn("Could not ping broker (is KubeMQ running?): {}", ex);
        }
    }
}

// Expected output:
// INFO  Ping response — host=<host> version=<version> uptime=<seconds>s
// INFO  Connection ping example completed.
