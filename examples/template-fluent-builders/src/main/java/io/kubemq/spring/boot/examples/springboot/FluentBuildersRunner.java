package io.kubemq.spring.boot.examples.springboot;

import io.kubemq.sdk.cq.CommandResponseMessage;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class FluentBuildersRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FluentBuildersRunner.class);

    private final KubeMQTemplate template;

    public FluentBuildersRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Thread.sleep(1500);

            template.newEvent("Tagged event")
                    .toChannel("spring-fluent.events")
                    .withTag("source", "fluent-builder")
                    .send();
            log.info("Sent event via fluent builder with tag");

            template.newQueueMessage("Delayed queue msg")
                    .toChannel("spring-fluent.queues")
                    .withDelay(Duration.ofSeconds(1))
                    .withExpiration(Duration.ofSeconds(30))
                    .send();
            log.info("Sent queue message via fluent builder with delay + expiration");

            CommandResponseMessage cmdResponse = template.newCommand("Fluent command")
                    .toChannel("spring-fluent.commands")
                    .withTimeout(Duration.ofSeconds(10))
                    .send();
            log.info("Command via fluent builder: executed={}", cmdResponse.isExecuted());

            QueryResponseMessage queryResponse = template.newQuery("Fluent query")
                    .toChannel("spring-fluent.queries")
                    .withTimeout(Duration.ofSeconds(10))
                    .withCacheKey("fluent-cache")
                    .withCacheTTL(Duration.ofMinutes(5))
                    .send();
            if (!queryResponse.isExecuted()) {
                log.warn("Query was not executed: {}", queryResponse.getError());
            } else {
                byte[] rawBody = queryResponse.getBody();
                String body = rawBody != null ? new String(rawBody, StandardCharsets.UTF_8) : "<empty>";
                log.info("Query via fluent builder: executed={} body={}", queryResponse.isExecuted(), body);
            }

            log.info("Fluent builders example completed.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
