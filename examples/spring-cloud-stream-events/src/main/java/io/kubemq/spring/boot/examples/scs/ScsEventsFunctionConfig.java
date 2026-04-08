package io.kubemq.spring.boot.examples.scs;

import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Defines a Spring Cloud Stream {@code Function} bean that processes KubeMQ events.
 * The bean name {@code uppercase} maps to the binding name in {@code application.yml}.
 */
@Configuration
public class ScsEventsFunctionConfig {

    private static final Logger log = LoggerFactory.getLogger(ScsEventsFunctionConfig.class);

    @Bean
    public Function<String, String> uppercase() {
        return payload -> {
            if (payload == null) {
                return null;
            }
            String result = payload.toUpperCase();
            log.info("SCS function: {} -> {}", payload, result);
            return result;
        };
    }
}
