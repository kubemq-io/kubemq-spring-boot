package io.kubemq.spring.boot.examples.queues;

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AckRejectRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AckRejectRunner.class);

    private final KubeMQTemplate template;

    public AckRejectRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            template.sendQueueMessage("spring-queues.ack-reject", "Valid order");
            template.sendQueueMessage("spring-queues.ack-reject", "INVALID");
            template.sendQueueMessage("spring-queues.ack-reject", "Another valid order");
            log.info("Sent 3 messages (1 invalid) to spring-queues.ack-reject");
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
