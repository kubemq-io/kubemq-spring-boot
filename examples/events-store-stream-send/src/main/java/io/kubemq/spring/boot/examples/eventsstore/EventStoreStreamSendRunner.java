package io.kubemq.spring.boot.examples.eventsstore;

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
public class EventStoreStreamSendRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EventStoreStreamSendRunner.class);

    private final KubeMQTemplate template;

    public EventStoreStreamSendRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            CountDownLatch latch = new CountDownLatch(10);
            for (int i = 1; i <= 10; i++) {
                final int idx = i;
                CompletableFuture<Void> future =
                        template.sendEventStoreAsync("spring-events-store.stream-send", "Async persistent #" + i);
                future.whenComplete((v, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send async persistent event #{}: {}", idx, ex.getMessage());
                    } else {
                        log.info("Async persistent event #{} sent successfully", idx);
                    }
                    latch.countDown();
                });
            }
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                log.warn("Not all async sends completed within timeout ({} remaining)", latch.getCount());
            }
            log.info("Events store stream send example completed — {} events sent asynchronously.", 10);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}

// Expected output:
// INFO  Async persistent event #1 sent successfully
// INFO  Async persistent event #2 sent successfully
// ...
// INFO  Async persistent event #10 sent successfully
// INFO  Events store stream send example completed — 10 events sent asynchronously.
