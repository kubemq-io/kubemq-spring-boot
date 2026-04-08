package io.kubemq.spring.boot.autoconfigure.listener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a KubeMQ query handler.
 *
 * <p>The annotated method receives {@link io.kubemq.sdk.cq.QueryMessageReceived} and must return
 * a {@link io.kubemq.sdk.cq.QueryResponseMessage}.
 *
 * <p>Uses singular {@code channel} because CQ handlers are point-to-point: each handler
 * services exactly one query channel.
 *
 * <pre>{@code
 * @KubeMQQueryHandler(channel = "queries.user-lookup")
 * public QueryResponseMessage handleQuery(QueryMessageReceived query) {
 *     return QueryResponseMessage.builder()
 *         .queryReceived(query)
 *         .body(lookupResult)
 *         .isExecuted(true)
 *         .build();
 * }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface KubeMQQueryHandler {

    /**
     * The single query channel to subscribe to. Supports SpEL and property placeholders.
     */
    String channel();

    /**
     * Consumer group for shared subscriptions.
     */
    String group() default "";

    /**
     * Number of concurrent query processors.
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
