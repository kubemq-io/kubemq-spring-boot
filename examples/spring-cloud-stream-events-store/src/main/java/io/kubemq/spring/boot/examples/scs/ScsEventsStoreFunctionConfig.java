package io.kubemq.spring.boot.examples.scs;

import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Defines a Spring Cloud Stream {@code Function} bean that processes KubeMQ events-store
 * messages. The bean name {@code transform} maps to the binding name in {@code application.yml}.
 */
@Configuration
public class ScsEventsStoreFunctionConfig {

    private static final Logger log = LoggerFactory.getLogger(ScsEventsStoreFunctionConfig.class);

    @Bean
    public Function<String, String> transform() {
        return payload -> {
            String result = payload.replace(" ", "-");
            log.info("SCS events-store function: {} -> {}", payload, result);
            return result;
        };
    }
}

// Expected output:
// INFO  StreamBridge send (events-store) result: true
// INFO  SCS events-store function: persistent event via SCS -> persistent-event-via-SCS
// INFO  Spring Cloud Stream events-store example completed.
