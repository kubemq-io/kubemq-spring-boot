package io.kubemq.spring.boot.examples.events;

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StreamSendRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StreamSendRunner.class);

    private final KubeMQTemplate template;

    public StreamSendRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            CountDownLatch latch = new CountDownLatch(10);
            for (int i = 1; i <= 10; i++) {
                final int idx = i;
                CompletableFuture<Void> future =
                        template.sendEventAsync("spring-events.stream-send", "Async event #" + i);
                future.whenComplete((v, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send async event #{}: {}", idx, ex.getMessage());
                    } else {
                        log.info("Async event #{} sent successfully", idx);
                    }
                    latch.countDown();
                });
            }
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                log.warn("Not all async sends completed within timeout ({} remaining)", latch.getCount());
            }
            log.info("Stream send example completed — {} events sent asynchronously.", 10);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}

// Expected output:
// INFO  Async event #1 sent successfully
// INFO  Async event #2 sent successfully
// ...
// INFO  Async event #10 sent successfully
// INFO  Stream send example completed — 10 events sent asynchronously.
