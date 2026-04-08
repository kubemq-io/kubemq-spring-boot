package io.kubemq.spring.boot.examples.commands;

import io.kubemq.sdk.cq.CommandMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQCommandHandler;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SendCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(SendCommandHandler.class);

    @KubeMQCommandHandler(channel = "spring-commands.send")
    public boolean onCommand(CommandMessageReceived cmd) {
        String body = (cmd.getBody() != null ? new String(cmd.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("Command received: channel={} body={}", cmd.getChannel(), body);
        return true;
    }
}

// Expected output:
// INFO  Command received: channel=spring-commands.send body=Restart service
// INFO  Command response: executed=true
// INFO  Send command example completed.
