package io.kubemq.spring.boot.autoconfigure.listener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a KubeMQ queue message listener.
 *
 * <p>The annotated method receives {@link io.kubemq.sdk.queues.QueueMessageReceived} (single)
 * or {@code List<QueueMessageReceived>} (batch mode) from the specified channels.
 *
 * <pre>{@code
 * @KubeMQQueueListener(channels = "queues.orders", autoAck = "true")
 * public void processOrder(QueueMessageReceived msg) { ... }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface KubeMQQueueListener {

    /**
     * Channel names to poll messages from. Supports SpEL and property placeholders.
     */
    String[] channels();

    /**
     * Number of concurrent poll loops.
     */
    String concurrency() default "";

    /**
     * Maximum time in seconds to wait for messages per poll cycle.
     */
    String pollTimeout() default "";

    /**
     * Maximum number of messages to return per poll.
     */
    String maxPollMessages() default "";

    /**
     * Duration in seconds that received messages remain hidden from other consumers.
     */
    String visibilityTimeout() default "";

    /**
     * Whether messages are automatically acknowledged upon receipt.
     */
    String autoAck() default "";

    /**
     * Whether the method receives a batch ({@code List<QueueMessageReceived>}).
     */
    String batch() default "false";

    /**
     * Bean name of a custom {@link KubeMQListenerContainerFactory}.
     */
    String containerFactory() default "";

    /**
     * Whether this listener auto-starts with the application context.
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
