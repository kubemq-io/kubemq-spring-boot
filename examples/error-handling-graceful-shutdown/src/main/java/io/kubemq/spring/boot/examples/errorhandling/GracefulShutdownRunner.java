package io.kubemq.spring.boot.examples.errorhandling;

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * Demonstrates Spring shutdown hooks ({@link SmartLifecycle}, {@link jakarta.annotation.PreDestroy}) in sequence.
 *
 * <p>This is for illustration of Spring lifecycle integration only — it does not perform
 * KubeMQ-specific draining of in-flight messages; use broker/client APIs for that where needed.
 */
@Component
public class GracefulShutdownRunner implements ApplicationRunner, SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(GracefulShutdownRunner.class);

    private final KubeMQTemplate template;
    private volatile boolean running = false;

    public GracefulShutdownRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            template.sendEvent("spring-error.graceful-shutdown", "Test event");
            log.info("Sent event — shutdown the app to see graceful shutdown");
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }

    @Override
    public void start() {
        running = true;
        log.info("SmartLifecycle started");
    }

    @Override
    public void stop() {
        // Spring SmartLifecycle hook only — not KubeMQ message draining (see class Javadoc).
        log.info("SmartLifecycle stop — draining in-flight messages");
        running = false;
        log.info("SmartLifecycle stopped — all messages drained");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 1;
    }

    @PreDestroy
    public void onDestroy() {
        log.info("@PreDestroy — cleaning up KubeMQ resources");
    }
}

// Expected output:
// INFO  SmartLifecycle started
// INFO  Sent event — shutdown the app to see graceful shutdown
// (SIGTERM / Ctrl+C)
// INFO  SmartLifecycle stop — draining in-flight messages
// INFO  SmartLifecycle stopped — all messages drained
// INFO  @PreDestroy — cleaning up KubeMQ resources
