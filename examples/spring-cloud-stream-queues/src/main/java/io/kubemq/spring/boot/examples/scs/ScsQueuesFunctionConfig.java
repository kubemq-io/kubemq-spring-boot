package io.kubemq.spring.boot.examples.scs;

import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Defines a Spring Cloud Stream {@code Function} bean that processes KubeMQ queue
 * messages. The bean name {@code process} maps to the binding name in {@code application.yml}.
 */
@Configuration
public class ScsQueuesFunctionConfig {

    private static final Logger log = LoggerFactory.getLogger(ScsQueuesFunctionConfig.class);

    @Bean
    public Function<String, String> process() {
        return payload -> {
            if (payload == null) {
                return null;
            }
            String result = "processed: " + payload;
            log.info("SCS queues function: {} -> {}", payload, result);
            return result;
        };
    }
}

// Expected output:
// INFO  StreamBridge send (queues) result: true
// INFO  SCS queues function: queue payload via SCS -> processed: queue payload via SCS
// INFO  Spring Cloud Stream queues example completed.
