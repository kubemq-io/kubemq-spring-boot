package io.kubemq.spring.boot.autoconfigure.listener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a KubeMQ event-store listener with replay capability.
 *
 * <p>The annotated method receives {@link io.kubemq.sdk.pubsub.EventStoreMessageReceived} instances.
 * The {@code subscriptionType} controls the replay starting point.
 *
 * <pre>{@code
 * @KubeMQEventStoreListener(
 *     channels = "events_store.orders",
 *     subscriptionType = "StartFromFirst"
 * )
 * public void replayOrders(EventStoreMessageReceived event) { ... }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface KubeMQEventStoreListener {

    /**
     * Channel names to subscribe to. Supports SpEL and property placeholders.
     */
    String[] channels();

    /**
     * Consumer group for shared subscriptions.
     */
    String group() default "";

    /**
     * Subscription start type. One of: StartNewOnly, StartFromFirst, StartFromLast,
     * StartAtSequence, StartAtTime, StartAtTimeDelta. Resolved at runtime.
     */
    String subscriptionType() default "StartNewOnly";

    /**
     * Value associated with the subscription type (e.g. sequence number, timestamp).
     * Resolved as long at runtime.
     */
    String subscriptionValue() default "0";

    /**
     * Number of concurrent message processors.
     */
    String concurrency() default "";

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
