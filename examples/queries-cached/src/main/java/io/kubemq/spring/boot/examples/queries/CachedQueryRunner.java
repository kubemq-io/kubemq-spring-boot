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
public class CachedQueryRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CachedQueryRunner.class);

    private final KubeMQTemplate template;

    public CachedQueryRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Thread.sleep(1500);
            QueryResponseMessage response = template.newQuery("Get config")
                    .toChannel("spring-queries.cached")
                    .withCacheKey("config-cache")
                    .withCacheTTL(Duration.ofMinutes(5))
                    .withTimeout(Duration.ofSeconds(30))
                    .send();
            if (!response.isExecuted()) {
                log.warn("Query was not executed: {}", response.getError());
            } else {
                byte[] rawBody = response.getBody();
                String body = rawBody != null ? new String(rawBody, StandardCharsets.UTF_8) : "<empty>";
                log.info("Cached query response: executed={} body={}", response.isExecuted(), body);
            }

            QueryResponseMessage cachedResponse = template.newQuery("Get config")
                    .toChannel("spring-queries.cached")
                    .withCacheKey("config-cache")
                    .withCacheTTL(Duration.ofMinutes(5))
                    .withTimeout(Duration.ofSeconds(30))
                    .send();
            if (!cachedResponse.isExecuted()) {
                log.warn("Cached query was not executed: {}", cachedResponse.getError());
            } else {
                byte[] rawCachedBody = cachedResponse.getBody();
                String cachedBody =
                        rawCachedBody != null ? new String(rawCachedBody, StandardCharsets.UTF_8) : "<empty>";
                log.info("Second query (from cache): executed={} body={}", cachedResponse.isExecuted(), cachedBody);
            }
            log.info("Cached query example completed.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
