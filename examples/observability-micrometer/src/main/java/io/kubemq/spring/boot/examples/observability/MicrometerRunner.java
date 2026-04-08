package io.kubemq.spring.boot.examples.observability;

import java.time.Duration;

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MicrometerRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MicrometerRunner.class);

    private static final long STARTUP_DELAY_MS = 2000L;
    private static final long BEFORE_PROMETHEUS_DELAY_MS = 1000L;
    private static final int EVENT_COUNT = 5;

    private final RestTemplate restTemplate = new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${server.port:8080}")
    private int serverPort;

    private final KubeMQTemplate template;

    public MicrometerRunner(KubeMQTemplate template) {
        this.template = template;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Thread.sleep(STARTUP_DELAY_MS);
            for (int i = 1; i <= EVENT_COUNT; i++) {
                template.sendEvent("spring-observability.micrometer", "Observed event #" + i);
                log.info("Sent observed event #{}", i);
            }

            Thread.sleep(BEFORE_PROMETHEUS_DELAY_MS);
            String baseUrl = "http://localhost:" + serverPort;
            String prometheusUrl = baseUrl + "/actuator/prometheus";
            String body = restTemplate.getForObject(prometheusUrl, String.class);
            if (body != null && body.contains("kubemq")) {
                log.info("KubeMQ metrics found in Prometheus output");
            } else {
                log.warn("KubeMQ metrics NOT found in Prometheus output — check metric registration");
            }
            log.info("Micrometer observability example — visit {}", prometheusUrl);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Example interrupted", ex);
        } catch (Exception ex) {
            log.warn("Could not query metrics endpoint: {}", ex);
        }
    }
}

// Expected output:
// INFO  Sent observed event #1
// ...
// INFO  Sent observed event #5
// INFO  KubeMQ metrics found in Prometheus output
// INFO  Micrometer observability example — visit http://localhost:8080/actuator/prometheus
