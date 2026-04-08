package io.kubemq.spring.boot.examples.commands;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Send Command Example
 *
 * <p>Demonstrates sending a command via KubeMQTemplate and handling it with
 * {@code @KubeMQCommandHandler} returning a simple boolean response.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends commands</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQCommandHandler} — receives and responds to commands</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class SendCommandApplication {

    public static void main(String[] args) {
        SpringApplication.run(SendCommandApplication.class, args);
    }
}
// Expected output:
// INFO  Command received: channel=spring-commands.send body=Restart service
