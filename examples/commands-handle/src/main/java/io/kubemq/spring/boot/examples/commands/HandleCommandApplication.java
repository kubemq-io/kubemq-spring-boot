package io.kubemq.spring.boot.examples.commands;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Handle Command Example
 *
 * <p>Demonstrates the handler side of commands — uses {@code @KubeMQCommandHandler}
 * returning {@code CommandResponseMessage} for full control over the response.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends commands</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQCommandHandler} — handles commands with full response control</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class HandleCommandApplication {

    public static void main(String[] args) {
        SpringApplication.run(HandleCommandApplication.class, args);
    }
}
// Expected output:
// INFO  Command handler received: channel=spring-commands.handle body=Deploy v2.0
