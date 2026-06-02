package io.kubemq.spring.boot.samples.springcloudstream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class ScsDemoRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ScsDemoRunner.class);

    private final StreamBridge streamBridge;

    public ScsDemoRunner(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            boolean sent = streamBridge.send("uppercase-in-0", "hello from scs");
            log.info("StreamBridge send to uppercase-in-0: {}", sent);
        } catch (Exception ex) {
            log.warn("StreamBridge demo failed: {}", ex.getMessage());
        }
    }
}
