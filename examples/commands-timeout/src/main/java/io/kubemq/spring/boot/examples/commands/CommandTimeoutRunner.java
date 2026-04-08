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
public class CommandTimeoutRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CommandTimeoutRunner.class);

    private final KubeMQTemplate template;

    public CommandTimeoutRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Thread.sleep(1500);
            CommandResponseMessage response = template.sendCommand(
                    "spring-commands.timeout", "Will timeout", Duration.ofSeconds(2));
            log.info("Command response: executed={}", response.isExecuted());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
            if (msg.contains("timeout") || msg.contains("deadline")) {
                log.info("Command timed out as expected: {}", ex.getMessage());
            } else {
                log.warn("Command failed (not a timeout — {}): {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
            }
        }
        log.info("Command timeout example completed.");
    }
}

// Expected output:
// WARN  Command timed out as expected: <timeout message>
// INFO  Command timeout example completed.
