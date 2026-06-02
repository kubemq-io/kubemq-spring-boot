package io.kubemq.spring.boot.samples.springcloudstream;

import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UppercaseFunctionConfig {

    private static final Logger log = LoggerFactory.getLogger(UppercaseFunctionConfig.class);

    @Bean
    public Function<String, String> uppercase() {
        return payload -> {
            String out = payload.toUpperCase();
            log.info("SCS uppercase: {} -> {}", payload, out);
            return out;
        };
    }
}
