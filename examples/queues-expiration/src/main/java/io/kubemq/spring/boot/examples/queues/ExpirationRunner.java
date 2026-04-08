package io.kubemq.spring.boot.examples.queues;

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ExpirationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ExpirationRunner.class);

    private final KubeMQTemplate template;

    public ExpirationRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            template.newQueueMessage("Expiring message")
                    .toChannel("spring-queues.expiration")
                    .withExpiration(Duration.ofSeconds(10))
                    .send();
            log.info("Sent message with 10s expiration to spring-queues.expiration");
            log.info("Expiration example — message will expire if not consumed within 10 seconds.");
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
