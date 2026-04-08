package io.kubemq.spring.boot.examples.scs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class ScsEventsStoreRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ScsEventsStoreRunner.class);

    private final StreamBridge streamBridge;

    public ScsEventsStoreRunner(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            boolean sent = streamBridge.send("transform-in-0", "persistent event via SCS");
            log.info("StreamBridge send (events-store) result: {}", sent);
            Thread.sleep(1000);
            log.info("Spring Cloud Stream events-store example completed.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
