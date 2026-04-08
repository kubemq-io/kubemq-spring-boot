package io.kubemq.spring.boot.examples.queries;

import io.kubemq.sdk.cq.QueryMessageReceived;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueryHandler;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HandleQueryHandler {

    private static final Logger log = LoggerFactory.getLogger(HandleQueryHandler.class);

    @KubeMQQueryHandler(channel = "spring-queries.handle")
    public QueryResponseMessage onQuery(QueryMessageReceived query) {
        String body = (query.getBody() != null ? new String(query.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("Query handler received: channel={} body={}", query.getChannel(), body);
        return QueryResponseMessage.builder()
                .queryReceived(query)
                .isExecuted(true)
                .body("{\"orderId\": \"ORD-123\", \"status\": \"shipped\"}".getBytes(StandardCharsets.UTF_8))
                .build();
    }
}

// Expected output:
// INFO  Query handler received: channel=spring-queries.handle body=Get order status
// INFO  Query response: executed=true body={"orderId": "ORD-123", "status": "shipped"}
// INFO  Handle query example completed.
