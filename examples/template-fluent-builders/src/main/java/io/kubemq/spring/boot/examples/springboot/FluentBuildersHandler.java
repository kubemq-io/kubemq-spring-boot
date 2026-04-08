package io.kubemq.spring.boot.examples.springboot;

import io.kubemq.sdk.cq.CommandMessageReceived;
import io.kubemq.sdk.cq.QueryMessageReceived;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQCommandHandler;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueryHandler;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FluentBuildersHandler {

    private static final Logger log = LoggerFactory.getLogger(FluentBuildersHandler.class);

    @KubeMQCommandHandler(channel = "spring-fluent.commands")
    public boolean onCommand(CommandMessageReceived cmd) {
        byte[] body = cmd.getBody();
        String text = body != null ? new String(body, StandardCharsets.UTF_8) : "<empty>";
        log.info("Fluent command received: {}", text);
        return true;
    }

    @KubeMQQueryHandler(channel = "spring-fluent.queries")
    public QueryResponseMessage onQuery(QueryMessageReceived query) {
        byte[] body = query.getBody();
        String text = body != null ? new String(body, StandardCharsets.UTF_8) : "<empty>";
        log.info("Fluent query received: {}", text);
        return QueryResponseMessage.builder()
                .queryReceived(query)
                .isExecuted(true)
                .body("{\"result\": \"fluent\"}".getBytes(StandardCharsets.UTF_8))
                .build();
    }
}

// Expected output:
// INFO  Sent event via fluent builder with tag
// INFO  Sent queue message via fluent builder with delay + expiration
// INFO  Fluent command received: Fluent command
// INFO  Command via fluent builder: executed=true
// INFO  Fluent query received: Fluent query
// INFO  Query via fluent builder: executed=true body={"result": "fluent"}
// INFO  Fluent builders example completed.
