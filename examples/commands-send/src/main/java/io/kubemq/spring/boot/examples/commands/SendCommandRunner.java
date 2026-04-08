package io.kubemq.spring.boot.examples.commands;

import io.kubemq.sdk.cq.CommandResponseMessage;
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SendCommandRunner implements ApplicationRunner {

    /** Delay to allow annotated listeners to subscribe before sending messages. */
    private static final long LISTENER_WARMUP_MS = 1500;

    private static final Logger log = LoggerFactory.getLogger(SendCommandRunner.class);

    private final KubeMQTemplate template;

    public SendCommandRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Thread.sleep(LISTENER_WARMUP_MS);
            CommandResponseMessage response = template.sendCommand(
                    "spring-commands.send", "Restart service", Duration.ofSeconds(30));
            log.info("Command response: executed={}", response.isExecuted());
            log.info("Send command example completed.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
