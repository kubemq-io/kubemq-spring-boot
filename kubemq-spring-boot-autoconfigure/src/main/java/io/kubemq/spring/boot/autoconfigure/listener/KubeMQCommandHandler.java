package io.kubemq.spring.boot.autoconfigure.listener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a KubeMQ command handler.
 *
 * <p>The annotated method receives {@link io.kubemq.sdk.cq.CommandMessageReceived} and returns
 * either {@code boolean} (simple mode) or {@link io.kubemq.sdk.cq.CommandResponseMessage}
 * (full control).
 *
 * <p>Uses singular {@code channel} because CQ handlers are point-to-point: each handler
 * services exactly one command channel.
 *
 * <pre>{@code
 * @KubeMQCommandHandler(channel = "commands.device-control", group = "handlers")
 * public boolean handleCommand(CommandMessageReceived cmd) {
 *     return true; // executed successfully
 * }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface KubeMQCommandHandler {

    /**
     * The single command channel to subscribe to. Supports SpEL and property placeholders.
     */
    String channel();

    /**
     * Consumer group for shared subscriptions.
     */
    String group() default "";

    /**
     * Number of concurrent command processors.
     */
    String concurrency() default "";

    /**
     * Bean name of a custom {@link KubeMQListenerContainerFactory}.
     */
    String containerFactory() default "";

    /**
     * Whether this handler auto-starts with the application context.
     */
    String autoStartup() default "";

    /**
     * Bean name of a custom {@link org.springframework.util.ErrorHandler}.
     */
    String errorHandler() default "";

    /**
     * Unique identifier for this listener endpoint.
     */
    String id() default "";
}
