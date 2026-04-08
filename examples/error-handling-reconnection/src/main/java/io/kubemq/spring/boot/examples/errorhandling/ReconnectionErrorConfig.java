package io.kubemq.spring.boot.examples.errorhandling;

import io.kubemq.spring.boot.autoconfigure.support.KubeMQErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Custom global error handler for the KubeMQ listener infrastructure.
 *
 * <p>Replaces the default error logging. When any annotated listener (for example
 * {@code @KubeMQEventListener}) throws, this handler is invoked: it logs the exception
 * type and message at ERROR level and the full stack trace at DEBUG.
 */
@Configuration
public class ReconnectionErrorConfig {

    private static final Logger log = LoggerFactory.getLogger(ReconnectionErrorConfig.class);

    @Bean
    public KubeMQErrorHandler kubemqErrorHandler() {
        return new KubeMQErrorHandler() {
            @Override
            public void handleError(Throwable t) {
                log.error("KubeMQ listener error [{}]: {}", t.getClass().getSimpleName(), t.getMessage());
                log.debug("Full stack trace for listener error", t);
            }
        };
    }
}
