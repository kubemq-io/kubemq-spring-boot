package io.kubemq.spring.boot.examples.eventsstore;

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class EventStoreConsumerGroupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EventStoreConsumerGroupRunner.class);

    private final KubeMQTemplate template;

    public EventStoreConsumerGroupRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Thread.sleep(500);
            for (int i = 1; i <= 6; i++) {
                template.sendEventStore("spring-events-store.consumer-group", "Persistent msg #" + i);
                log.info("Sent persistent event #{} to spring-events-store.consumer-group", i);
            }
            Thread.sleep(1000);
            log.info("Events store consumer group example completed.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
