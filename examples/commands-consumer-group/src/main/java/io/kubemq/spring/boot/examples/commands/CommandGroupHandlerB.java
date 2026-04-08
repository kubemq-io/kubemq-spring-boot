package io.kubemq.spring.boot.examples.commands;

import io.kubemq.sdk.cq.CommandMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQCommandHandler;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Paired with CommandGroupHandlerA to demonstrate consumer group load balancing. Both handlers are identical except for the log prefix.
@Component
public class CommandGroupHandlerB {

    private static final Logger log = LoggerFactory.getLogger(CommandGroupHandlerB.class);

    @KubeMQCommandHandler(channel = "spring-commands.consumer-group", group = "cmd-group")
    public boolean onCommand(CommandMessageReceived cmd) {
        String body = (cmd.getBody() != null ? new String(cmd.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("[Handler-B] Command received: {}", body);
        return true;
    }
}

// Expected output:
// INFO  [Handler-A] Command received: Command #1
// INFO  Command #1 response: executed=true
// INFO  [Handler-B] Command received: Command #2
// INFO  Command #2 response: executed=true
// ...
// INFO  Command consumer group example completed.
