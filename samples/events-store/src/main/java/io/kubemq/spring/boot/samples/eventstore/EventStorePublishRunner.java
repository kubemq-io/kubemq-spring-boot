package io.kubemq.spring.boot.samples.eventstore;

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class EventStorePublishRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EventStorePublishRunner.class);

    private final KubeMQTemplate template;

    public EventStorePublishRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            template.sendEventStore("sample.events_store", "persisted event from events-store sample");
            log.info("Published demo event-store message");
        } catch (Exception ex) {
            log.warn("Could not publish event-store message: {}", ex.getMessage());
        }
    }
}
