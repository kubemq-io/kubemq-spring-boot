package io.kubemq.spring.boot.examples.eventsstore;

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ReplayFromTimeRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ReplayFromTimeRunner.class);

    private final KubeMQTemplate template;

    public ReplayFromTimeRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Thread.sleep(500);
            for (int i = 1; i <= 5; i++) {
                template.sendEventStore("spring-events-store.replay-time", "Time-replay event #" + i);
                log.info("Published event #{} to spring-events-store.replay-time", i);
            }
            Thread.sleep(1000);
            log.info("Replay-from-time example completed.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
