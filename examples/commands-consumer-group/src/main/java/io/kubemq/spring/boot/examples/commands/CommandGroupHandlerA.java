package io.kubemq.spring.boot.examples.commands;

import io.kubemq.sdk.cq.CommandMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQCommandHandler;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Paired with CommandGroupHandlerB to demonstrate consumer group load balancing. Both handlers are identical except for the log prefix.
@Component
public class CommandGroupHandlerA {

    private static final Logger log = LoggerFactory.getLogger(CommandGroupHandlerA.class);

    @KubeMQCommandHandler(channel = "spring-commands.consumer-group", group = "cmd-group")
    public boolean onCommand(CommandMessageReceived cmd) {
        String body = (cmd.getBody() != null ? new String(cmd.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("[Handler-A] Command received: {}", body);
        return true;
    }
}
