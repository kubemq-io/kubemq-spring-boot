package io.kubemq.spring.boot.examples.eventsstore;

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BasicEventStoreRunner implements ApplicationRunner {

    /** Delay to allow annotated listeners to subscribe before sending messages. */
    private static final long LISTENER_WARMUP_MS = 500;

    /** Delay after publishing so delivery can complete before the example exits. */
    private static final long POST_SEND_DRAIN_MS = 1000;

    private static final Logger log = LoggerFactory.getLogger(BasicEventStoreRunner.class);

    private final KubeMQTemplate template;

    public BasicEventStoreRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Thread.sleep(LISTENER_WARMUP_MS);
            for (int i = 1; i <= 3; i++) {
                template.sendEventStore("spring-events-store.basic", "Persistent event #" + i);
                log.info("Published persistent event #{} to spring-events-store.basic", i);
            }
            Thread.sleep(POST_SEND_DRAIN_MS);
            log.info("Basic event store example completed.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
