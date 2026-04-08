package io.kubemq.spring.boot.examples.queues;

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BatchSendRunner implements ApplicationRunner {

    /** Delay to allow annotated listeners to subscribe before sending messages. */
    private static final long LISTENER_WARMUP_MS = 500;

    /** Delay after sending so the listener can process before the example exits. */
    private static final long POST_SEND_DRAIN_MS = 1000;

    private static final Logger log = LoggerFactory.getLogger(BatchSendRunner.class);

    private final KubeMQTemplate template;

    public BatchSendRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Thread.sleep(LISTENER_WARMUP_MS);
            template.sendQueueMessages("spring-queues.batch-send",
                    List.of("Batch msg #1", "Batch msg #2", "Batch msg #3"));
            log.info("Sent 3 messages in batch to spring-queues.batch-send");
            Thread.sleep(POST_SEND_DRAIN_MS);
            log.info("Batch send example — messages sent, listener will process them.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}
