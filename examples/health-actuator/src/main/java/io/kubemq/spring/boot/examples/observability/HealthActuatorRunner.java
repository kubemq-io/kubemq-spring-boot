package io.kubemq.spring.boot.examples.observability;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HealthActuatorRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(HealthActuatorRunner.class);

    private static final long STARTUP_DELAY_MS = 2000L;

    private final RestTemplate restTemplate = new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${server.port:8080}")
    private int serverPort;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Thread.sleep(STARTUP_DELAY_MS);
            String baseUrl = "http://localhost:" + serverPort;
            String healthUrl = baseUrl + "/actuator/health/kubemq";
            String health = restTemplate.getForObject(healthUrl, String.class);
            log.info("KubeMQ health endpoint: {}", health);
            log.info("Health actuator example — visit {}", healthUrl);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not query health endpoint: {}", ex);
        }
    }
}

// Expected output:
// INFO  KubeMQ health endpoint: {"status":"UP","details":{"host":"...","version":"..."}}
// INFO  Health actuator example — visit http://localhost:8080/actuator/health/kubemq
