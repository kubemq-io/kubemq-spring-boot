package io.kubemq.spring.boot.examples.commands;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Command Consumer Group Example
 *
 * <p>Demonstrates load-balanced command handling using consumer groups. Two handler
 * beans share the same {@code group} so each command is delivered to only one handler.
 *
 * <p><b>Spring Boot features used:</b>
 * <ul>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate} — sends commands</li>
 *   <li>{@link io.kubemq.spring.boot.autoconfigure.listener.KubeMQCommandHandler} — load-balanced command handling via consumer groups</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>KubeMQ broker running at localhost:50000 (or set KUBEMQ_ADDRESS)</li>
 * </ul>
 */
@SpringBootApplication
public class CommandConsumerGroupApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommandConsumerGroupApplication.class, args);
    }
}
// Expected output:
// INFO  [Handler-A] Command received: Command #1
