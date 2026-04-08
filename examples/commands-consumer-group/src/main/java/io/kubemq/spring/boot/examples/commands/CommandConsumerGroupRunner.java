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
public class CommandConsumerGroupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CommandConsumerGroupRunner.class);

    private final KubeMQTemplate template;

    public CommandConsumerGroupRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Thread.sleep(1500);
            for (int i = 1; i <= 4; i++) {
                CommandResponseMessage response = template.sendCommand(
                        "spring-commands.consumer-group", "Command #" + i, Duration.ofSeconds(30));
                log.info("Command #{} response: executed={}", i, response.isExecuted());
            }
            log.info("Command consumer group example completed.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
