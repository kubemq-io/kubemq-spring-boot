package io.kubemq.spring.boot.examples.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Autoconfigure Profiles Example
 *
 * <p>Demonstrates multi-profile configuration with Spring profiles.
 * Different YAML configurations are loaded based on the active profile
 * ({@code dev}, {@code staging}, {@code prod}).
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>Spring profiles — environment-specific KubeMQ configuration</li>
 *   <li>{@code @Value} injection with profile overrides</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class AutoconfigureProfilesApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoconfigureProfilesApplication.class, args);
    }
}
// Expected output:
// INFO  Active profiles: dev
