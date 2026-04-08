package io.kubemq.spring.boot.examples.queues;

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DelayedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DelayedRunner.class);

    private final KubeMQTemplate template;

    public DelayedRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            template.newQueueMessage("Delayed message")
                    .toChannel("spring-queues.delayed")
                    .withDelay(Duration.ofSeconds(5))
                    .send();
            log.info("Sent delayed message (5s delay) to spring-queues.delayed");
            log.info("Delayed queue example — message will be visible to consumers after 5 seconds.");
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
