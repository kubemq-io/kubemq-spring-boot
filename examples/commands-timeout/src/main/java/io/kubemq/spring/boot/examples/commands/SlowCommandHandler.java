package io.kubemq.spring.boot.examples.commands;

import io.kubemq.sdk.cq.CommandMessageReceived;
import io.kubemq.sdk.cq.CommandResponseMessage;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQCommandHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SlowCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(SlowCommandHandler.class);

    private static final int SLEEP_SECONDS = 5;

    @KubeMQCommandHandler(channel = "spring-commands.timeout")
    public CommandResponseMessage onCommand(CommandMessageReceived cmd) {
        String body = (cmd.getBody() != null ? new String(cmd.getBody(), StandardCharsets.UTF_8) : "<empty>");
        log.info("Slow handler started: channel={} body={}", cmd.getChannel(), body);
        try {
            TimeUnit.SECONDS.sleep(SLEEP_SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Slow handler interrupted", ex);
            return CommandResponseMessage.builder()
                    .commandReceived(cmd)
                    .isExecuted(false)
                    .build();
        }
        log.info("Slow handler finished processing");
        return CommandResponseMessage.builder()
                .commandReceived(cmd)
                .isExecuted(true)
                .build();
    }
}
