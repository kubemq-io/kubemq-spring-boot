package io.kubemq.spring.boot.samples.requestresponse;

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
public class SampleCqHandlers {

    private static final Logger log = LoggerFactory.getLogger(SampleCqHandlers.class);

    @KubeMQCommandHandler(channel = "sample.commands", group = "cq-demo")
    public boolean onCommand(CommandMessageReceived cmd) {
        log.info("Command received on {}", cmd.getChannel());
        return true;
    }

    @KubeMQQueryHandler(channel = "sample.queries", group = "cq-demo")
    public QueryResponseMessage onQuery(QueryMessageReceived query) {
        log.info("Query received on {}", query.getChannel());
        return QueryResponseMessage.builder()
                .queryReceived(query)
                .body("answer".getBytes(StandardCharsets.UTF_8))
                .isExecuted(true)
                .build();
    }
}
