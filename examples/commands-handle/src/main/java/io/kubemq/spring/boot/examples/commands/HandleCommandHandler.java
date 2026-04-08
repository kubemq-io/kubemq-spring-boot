package io.kubemq.spring.boot.examples.commands;

import io.kubemq.sdk.cq.CommandMessageReceived;
import io.kubemq.sdk.cq.CommandResponseMessage;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQCommandHandler;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HandleCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(HandleCommandHandler.class);

    @KubeMQCommandHandler(channel = "spring-commands.handle")
    public CommandResponseMessage onCommand(CommandMessageReceived cmd) {
        byte[] body = cmd.getBody();
        String text = body != null ? new String(body, StandardCharsets.UTF_8) : "<empty>";
        log.info("Command handler received: channel={} body={}", cmd.getChannel(), text);
        return CommandResponseMessage.builder()
                .commandReceived(cmd)
                .isExecuted(true)
                .build();
    }
}

// Expected output:
// INFO  Command handler received: channel=spring-commands.handle body=Deploy v2.0
// INFO  Command response: executed=true
// INFO  Handle command example completed.
