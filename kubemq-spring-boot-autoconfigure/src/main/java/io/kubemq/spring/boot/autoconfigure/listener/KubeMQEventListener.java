package io.kubemq.spring.boot.autoconfigure.listener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a KubeMQ event listener.
 *
 * <p>The annotated method receives {@link io.kubemq.sdk.pubsub.EventMessageReceived} instances
 * from the specified channels. All attributes support SpEL expressions and property placeholders.
 *
 * <pre>{@code
 * @KubeMQEventListener(channels = "${kubemq.channels.orders}", group = "order-group")
 * public void onOrder(EventMessageReceived event) { ... }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface KubeMQEventListener {

    /**
     * Channel names to subscribe to. Supports SpEL and property placeholders.
     */
    String[] channels();

    /**
     * Consumer group for shared subscriptions. Empty string means no group.
     */
    String group() default "";

    /**
     * Number of concurrent message processors. Resolved as integer at runtime.
     */
    String concurrency() default "";

    /**
     * Bean name of a custom {@link KubeMQListenerContainerFactory}.
     */
    String containerFactory() default "";

    /**
     * Whether this listener auto-starts with the application context. Resolved as boolean.
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
