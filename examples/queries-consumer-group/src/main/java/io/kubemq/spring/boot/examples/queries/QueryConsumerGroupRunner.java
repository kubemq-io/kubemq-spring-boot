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
public class QueryConsumerGroupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(QueryConsumerGroupRunner.class);

    private final KubeMQTemplate template;

    public QueryConsumerGroupRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Thread.sleep(1500);
            for (int i = 1; i <= 4; i++) {
                QueryResponseMessage response = template.sendQuery(
                        "spring-queries.consumer-group", "Query #" + i, Duration.ofSeconds(30));
                if (!response.isExecuted()) {
                    log.warn("Query #{} was not executed: {}", i, response.getError());
                } else {
                    byte[] rawBody = response.getBody();
                    String body = rawBody != null ? new String(rawBody, StandardCharsets.UTF_8) : "<empty>";
                    log.info("Query #{} response: executed={} body={}", i, response.isExecuted(), body);
                }
            }
            log.info("Query consumer group example completed.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
