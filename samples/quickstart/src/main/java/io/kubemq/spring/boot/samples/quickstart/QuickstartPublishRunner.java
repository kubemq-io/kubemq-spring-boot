package io.kubemq.spring.boot.samples.quickstart;

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class QuickstartPublishRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(QuickstartPublishRunner.class);

    private final KubeMQTemplate template;

    public QuickstartPublishRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            template.sendEvent("sample.events", "hello from KubeMQ Spring Boot quickstart");
            log.info("Published demo event to sample.events");
        } catch (Exception ex) {
            log.warn("Could not publish demo event (is KubeMQ running at {}?): {}",
                    System.getenv().getOrDefault("KUBEMQ_ADDRESS", "localhost:50000"),
                    ex.getMessage());
        }
    }
}
