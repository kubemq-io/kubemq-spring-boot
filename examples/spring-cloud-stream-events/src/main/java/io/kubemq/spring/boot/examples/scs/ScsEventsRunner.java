package io.kubemq.spring.boot.examples.scs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class ScsEventsRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ScsEventsRunner.class);

    private final StreamBridge streamBridge;

    public ScsEventsRunner(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            boolean sent = streamBridge.send("uppercase-in-0", "hello from spring cloud stream");
            log.info("StreamBridge send result: {}", sent);
            Thread.sleep(1000);
            log.info("Spring Cloud Stream events example completed.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}

// Expected output:
// INFO  StreamBridge send result: true
// INFO  SCS function: hello from spring cloud stream -> HELLO FROM SPRING CLOUD STREAM
// INFO  Spring Cloud Stream events example completed.
