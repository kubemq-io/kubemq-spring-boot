package io.kubemq.spring.boot.examples.queries;

import io.kubemq.sdk.cq.QueryMessageReceived;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueryHandler;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SendQueryHandler {

    private static final Logger log = LoggerFactory.getLogger(SendQueryHandler.class);

    @KubeMQQueryHandler(channel = "spring-queries.send")
    public QueryResponseMessage onQuery(QueryMessageReceived query) {
        byte[] body = query.getBody();
        String text = body != null ? new String(body, StandardCharsets.UTF_8) : "<empty>";
        log.info("Query received: channel={} body={}", query.getChannel(), text);
        return QueryResponseMessage.builder()
                .queryReceived(query)
                .isExecuted(true)
                .body("{\"user\": \"john\", \"role\": \"admin\"}".getBytes(StandardCharsets.UTF_8))
                .build();
    }
}

// Expected output:
// INFO  Query received: channel=spring-queries.send body=Get user data
// INFO  Query response: executed=true body={"user": "john", "role": "admin"}
// INFO  Send query example completed.
