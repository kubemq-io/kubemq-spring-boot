package io.kubemq.spring.boot.examples.queries;

import io.kubemq.sdk.cq.QueryMessageReceived;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueryHandler;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CachedQueryHandler {

    private static final Logger log = LoggerFactory.getLogger(CachedQueryHandler.class);

    @KubeMQQueryHandler(channel = "spring-queries.cached")
    public QueryResponseMessage onQuery(QueryMessageReceived query) {
        String body = (query.getBody() != null ? new String(query.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("Cache handler invoked (first call only): body={}", body);
        return QueryResponseMessage.builder()
                .queryReceived(query)
                .isExecuted(true)
                .body("{\"maxRetries\": 3, \"timeout\": 30}".getBytes(StandardCharsets.UTF_8))
                .build();
    }
}

// Expected output:
// INFO  Cache handler invoked (first call only): body=Get config
// INFO  Cached query response: executed=true body={"maxRetries": 3, "timeout": 30}
// INFO  Second query (from cache): executed=true body={"maxRetries": 3, "timeout": 30}
// INFO  Cached query example completed.
