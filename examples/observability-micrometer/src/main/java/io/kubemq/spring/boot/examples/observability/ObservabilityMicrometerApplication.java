package io.kubemq.spring.boot.examples.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Observability Micrometer Example
 *
 * <p>Demonstrates KubeMQTemplate observation tracing and metrics via
 * {@link io.micrometer.observation.ObservationRegistry}. Metrics are exported
 * to Prometheus via the {@code /actuator/prometheus} endpoint.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>Micrometer Observation API — automatic tracing of KubeMQ operations</li>
 *   <li>Prometheus metrics registry — {@code /actuator/prometheus} endpoint</li>
 *   <li>{@code kubemq.template.observation-enabled=true} — enables observation</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class ObservabilityMicrometerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObservabilityMicrometerApplication.class, args);
    }
}
// Expected output:
// INFO  Sent observed event #1
