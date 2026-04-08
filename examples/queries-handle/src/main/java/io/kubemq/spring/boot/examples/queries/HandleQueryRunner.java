package io.kubemq.spring.boot.examples.queries;

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
public class HandleQueryRunner implements ApplicationRunner {

    /** Delay to allow annotated listeners to subscribe before sending messages. */
    private static final long LISTENER_WARMUP_MS = 1500;

    private static final Logger log = LoggerFactory.getLogger(HandleQueryRunner.class);

    private final KubeMQTemplate template;

    public HandleQueryRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Thread.sleep(LISTENER_WARMUP_MS);
            QueryResponseMessage response = template.sendQuery(
                    "spring-queries.handle", "Get order status", Duration.ofSeconds(30));
            if (!response.isExecuted()) {
                log.warn("Query was not executed: {}", response.getError());
            } else {
                byte[] rawBody = response.getBody();
                String body = rawBody != null ? new String(rawBody, StandardCharsets.UTF_8) : "<empty>";
                log.info("Query response: executed={} body={}", response.isExecuted(), body);
            }
            log.info("Handle query example completed.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
