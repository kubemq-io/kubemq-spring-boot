package io.kubemq.spring.boot.examples.queues;

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterRunner.class);

    private final KubeMQTemplate template;

    public DeadLetterRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            template.newQueueMessage("DLQ test message")
                    .toChannel("spring-queues.dead-letter")
                    .withDeadLetterQueue("spring-queues.dead-letter.dlq", 3)
                    .send();
            log.info("Sent message with DLQ config (max 3 attempts) to spring-queues.dead-letter");
            log.info("Dead letter example — reject the message 3 times and it moves to DLQ.");
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
