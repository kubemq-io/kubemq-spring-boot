package io.kubemq.spring.boot.examples.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Health Actuator Example
 *
 * <p>Demonstrates the KubeMQ health indicator exposed via Spring Boot Actuator.
 * The {@code /actuator/health/kubemq} endpoint reports broker connectivity status.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>Spring Boot Actuator — health endpoint with KubeMQ indicator</li>
 *   <li>{@code management.endpoint.health.show-details=always} — full health details</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class HealthActuatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthActuatorApplication.class, args);
    }
}
// Expected output:
// INFO  KubeMQ health endpoint: {"status":"UP","details":{"host":"...","version":"..."}}
