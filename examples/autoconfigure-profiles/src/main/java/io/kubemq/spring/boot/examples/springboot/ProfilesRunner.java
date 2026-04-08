package io.kubemq.spring.boot.examples.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProfilesRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProfilesRunner.class);

    private final Environment env;

    @Value("${kubemq.address}")
    private String address;

    @Value("${kubemq.client-id}")
    private String clientId;

    public ProfilesRunner(Environment env) {
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {
        String[] profiles = env.getActiveProfiles();
        log.info("Active profiles: {}", String.join(", ", profiles));
        log.info("KubeMQ address: {}", address);
        log.info("KubeMQ client-id: {}", clientId);
        log.info("Profiles example completed.");
    }
}

// Expected output (with dev profile):
// INFO  Active profiles: dev
// INFO  KubeMQ address: localhost:50000
// INFO  KubeMQ client-id: spring-profiles-dev
// INFO  Profiles example completed.
