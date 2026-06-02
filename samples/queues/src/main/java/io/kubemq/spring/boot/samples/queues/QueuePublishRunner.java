package io.kubemq.spring.boot.samples.queues;

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class QueuePublishRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(QueuePublishRunner.class);

    private final KubeMQTemplate template;

    public QueuePublishRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            template.sendQueueMessages(
                    "sample.queue", List.of("queue-msg-1", "queue-msg-2", "queue-msg-3"));
            log.info("Published 3 queue messages to sample.queue");
        } catch (Exception ex) {
            log.warn("Could not publish queue messages: {}", ex.getMessage());
        }
    }
}
