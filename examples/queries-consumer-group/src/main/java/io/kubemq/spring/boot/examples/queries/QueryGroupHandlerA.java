package io.kubemq.spring.boot.examples.queries;

import io.kubemq.sdk.cq.QueryMessageReceived;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueryHandler;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Paired with QueryGroupHandlerB to demonstrate consumer group load balancing. Both handlers are identical except for the log prefix.
@Component
public class QueryGroupHandlerA {

    private static final Logger log = LoggerFactory.getLogger(QueryGroupHandlerA.class);

    @KubeMQQueryHandler(channel = "spring-queries.consumer-group", group = "query-group")
    public QueryResponseMessage onQuery(QueryMessageReceived query) {
        String body = (query.getBody() != null ? new String(query.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("[Handler-A] Query received: {}", body);
        return QueryResponseMessage.builder()
                .queryReceived(query)
                .isExecuted(true)
                .body("[A] processed".getBytes(StandardCharsets.UTF_8))
                .build();
    }
}
