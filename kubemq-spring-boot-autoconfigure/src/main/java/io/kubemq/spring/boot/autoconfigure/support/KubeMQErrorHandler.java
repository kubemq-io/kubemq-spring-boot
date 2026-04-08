package io.kubemq.spring.boot.autoconfigure.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ErrorHandler;

/**
 * Default {@link ErrorHandler} for KubeMQ listener containers.
 *
 * <p>Logs the error at ERROR level. Applications can replace this bean with a custom
 * implementation for circuit-breaking, dead-lettering, or alerting.
 */
public class KubeMQErrorHandler implements ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(KubeMQErrorHandler.class);

    @Override
    public void handleError(Throwable t) {
        log.error("KubeMQ listener error: {}", t.getMessage(), t);
    }
}
