package io.kubemq.spring.boot.examples.queries;

import io.kubemq.sdk.cq.QueryMessageReceived;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueryHandler;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Paired with QueryGroupHandlerA to demonstrate consumer group load balancing. Both handlers are identical except for the log prefix.
@Component
public class QueryGroupHandlerB {

    private static final Logger log = LoggerFactory.getLogger(QueryGroupHandlerB.class);

    @KubeMQQueryHandler(channel = "spring-queries.consumer-group", group = "query-group")
    public QueryResponseMessage onQuery(QueryMessageReceived query) {
        String body = (query.getBody() != null ? new String(query.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("[Handler-B] Query received: {}", body);
        return QueryResponseMessage.builder()
                .queryReceived(query)
                .isExecuted(true)
                .body("[B] processed".getBytes(StandardCharsets.UTF_8))
                .build();
    }
}

// Expected output:
// INFO  [Handler-A] Query received: Query #1
// INFO  Query #1 response: executed=true body=[A] processed
// INFO  [Handler-B] Query received: Query #2
// INFO  Query #2 response: executed=true body=[B] processed
// ...
// INFO  Query consumer group example completed.
