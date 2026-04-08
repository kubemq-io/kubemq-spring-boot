package io.kubemq.spring.boot.examples.events;

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class WildcardSubscribeRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(WildcardSubscribeRunner.class);

    private final KubeMQTemplate template;

    public WildcardSubscribeRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Thread.sleep(500);
            template.sendEvent("spring-events.orders.us", "US order");
            log.info("Sent event to spring-events.orders.us");
            template.sendEvent("spring-events.orders.eu", "EU order");
            log.info("Sent event to spring-events.orders.eu");
            template.sendEvent("spring-events.orders.asia", "Asia order");
            log.info("Sent event to spring-events.orders.asia");
            Thread.sleep(1000);
            log.info("Wildcard subscribe example completed.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
