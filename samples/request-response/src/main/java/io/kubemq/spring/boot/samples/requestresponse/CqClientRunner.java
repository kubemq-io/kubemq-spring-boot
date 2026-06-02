package io.kubemq.spring.boot.samples.requestresponse;

import io.kubemq.sdk.cq.CommandResponseMessage;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class CqClientRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CqClientRunner.class);

    private final KubeMQTemplate template;

    public CqClientRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        Thread.sleep(1500);
        try {
            CommandResponseMessage cmdResp =
                    template.sendCommand("sample.commands", "ping", Duration.ofSeconds(10));
            log.info("Command response executed={}", cmdResp.isExecuted());
        } catch (Exception ex) {
            log.warn("Command demo failed: {}", ex.getMessage());
        }
        try {
            QueryResponseMessage qResp =
                    template.sendQuery("sample.queries", "q", Duration.ofSeconds(10));
            String body = new String(qResp.getBody(), StandardCharsets.UTF_8);
            log.info("Query response executed={} body={}", qResp.isExecuted(), body);
        } catch (Exception ex) {
            log.warn("Query demo failed: {}", ex.getMessage());
        }
    }
}
