package io.kubemq.spring.boot.examples.commands;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Command Timeout Example
 *
 * <p>Demonstrates command timeout behavior by sending a command with a short client timeout
 * while {@link SlowCommandHandler} sleeps longer than that timeout, showing how to catch the timeout exception.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends commands with configurable timeout</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQCommandHandler} — via {@link SlowCommandHandler}</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class CommandTimeoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommandTimeoutApplication.class, args);
    }
}
// Expected output:
// WARN  Command timed out as expected: <timeout message>
