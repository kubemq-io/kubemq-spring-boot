package io.kubemq.spring.boot.examples.scs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class ScsQueuesRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ScsQueuesRunner.class);

    private final StreamBridge streamBridge;

    public ScsQueuesRunner(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            boolean sent = streamBridge.send("process-in-0", "queue payload via SCS");
            log.info("StreamBridge send (queues) result: {}", sent);
            Thread.sleep(1000);
            log.info("Spring Cloud Stream queues example completed.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
