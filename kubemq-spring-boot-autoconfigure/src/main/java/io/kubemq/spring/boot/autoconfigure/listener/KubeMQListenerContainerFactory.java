package io.kubemq.spring.boot.autoconfigure.listener;

import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter;
import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import io.kubemq.spring.boot.autoconfigure.support.KubeMQCoroutineBridge;
import java.time.Duration;
import org.springframework.util.ErrorHandler;

/**
 * Factory that creates {@link KubeMQMessageListenerContainer} instances from
 * {@link KubeMQListenerEndpoint} descriptors.
 *
 * <p>Applies default configuration from {@link KubeMQProperties} when annotation
 * attributes are not explicitly set.
 */
public class KubeMQListenerContainerFactory {

    private final PubSubClient pubSubClient;
    private final QueuesClient queuesClient;
    private final CQClient cqClient;
    private final KubeMQProperties properties;
    private final ErrorHandler errorHandler;
    private final KubeMQMessageConverter messageConverter;
    private final KubeMQCoroutineBridge coroutineSupport; // nullable — absent when kotlin module not on classpath

    public KubeMQListenerContainerFactory(
            PubSubClient pubSubClient,
            QueuesClient queuesClient,
            CQClient cqClient,
            KubeMQProperties properties,
            ErrorHandler errorHandler,
            KubeMQMessageConverter messageConverter,
            KubeMQCoroutineBridge coroutineSupport) {
        this.pubSubClient = pubSubClient;
        this.queuesClient = queuesClient;
        this.cqClient = cqClient;
        this.properties = properties;
        this.errorHandler = errorHandler;
        this.messageConverter = messageConverter;
        this.coroutineSupport = coroutineSupport;
    }

    /**
     * Creates a container for the given endpoint, applying property defaults for any
     * unset annotation attributes. Uses the factory's default error handler.
     */
    public KubeMQMessageListenerContainer createContainer(KubeMQListenerEndpoint endpoint) {
        return createContainer(endpoint, null);
    }

    /**
     * Creates a container for the given endpoint with an optional error handler override.
     * If {@code errorHandlerOverride} is non-null it takes precedence over the factory default.
     */
    public KubeMQMessageListenerContainer createContainer(
            KubeMQListenerEndpoint endpoint, ErrorHandler errorHandlerOverride) {
        MethodKubeMQListenerEndpoint methodEndpoint = applyDefaults(endpoint);
        ErrorHandler handler = errorHandlerOverride != null ? errorHandlerOverride : this.errorHandler;
        Duration shutdownTimeout = properties.getListener().getShutdownTimeout();
        return new KubeMQMessageListenerContainer(
                methodEndpoint, pubSubClient, queuesClient, cqClient,
                handler, messageConverter, coroutineSupport, shutdownTimeout);
    }

    /**
     * Applies property-level defaults for any fields left at their annotation zero-value.
     * Returns a new endpoint with defaults filled in.
     */
    private MethodKubeMQListenerEndpoint applyDefaults(KubeMQListenerEndpoint endpoint) {
        if (endpoint instanceof MethodKubeMQListenerEndpoint me) {
            MethodKubeMQListenerEndpoint.Builder builder = MethodKubeMQListenerEndpoint.builder()
                    .id(me.getId())
                    .type(me.getType())
                    .channels(me.getChannels())
                    .group(me.getGroup())
                    .bean(me.getBean())
                    .method(me.getMethod())
                    .errorHandlerBeanName(me.getErrorHandlerBeanName())
                    .containerFactoryBeanName(me.getContainerFactoryBeanName())
                    .eventsStoreType(me.getEventsStoreType())
                    .eventsStoreValue(me.getEventsStoreValue())
                    .batch(me.isBatch());

            int concurrency = me.getConcurrency() > 0
                    ? me.getConcurrency()
                    : properties.getListener().getConcurrency();
            builder.concurrency(concurrency);

            // Global auto-startup override: kubemq.listener.auto-startup=false
            // disables all containers, even if the per-annotation autoStartup=true.
            boolean autoStartup = me.isAutoStartup() && properties.getListener().isAutoStartup();
            builder.autoStartup(autoStartup);

            if (me.getType() == KubeMQListenerEndpoint.KubeMQListenerType.QUEUE) {
                int pollTimeout = me.getPollTimeoutSeconds() > 0
                        ? me.getPollTimeoutSeconds()
                        : (int) properties.getListener().getQueues().getPollTimeout().toSeconds();
                int maxPoll = me.getMaxPollMessages() > 0
                        ? me.getMaxPollMessages()
                        : properties.getListener().getQueues().getMaxPollMessages();
                int visibility = me.getVisibilityTimeoutSeconds() > 0
                        ? me.getVisibilityTimeoutSeconds()
                        : (int) properties.getListener().getQueues().getVisibilityTimeout().toSeconds();
                boolean autoAck = me.isAutoAck()
                        || properties.getListener().getQueues().isAutoAck();

                builder.pollTimeoutSeconds(pollTimeout)
                        .maxPollMessages(maxPoll)
                        .visibilityTimeoutSeconds(visibility)
                        .autoAck(autoAck);
            }

            return builder.build();
        }
        throw new IllegalArgumentException("Unsupported endpoint type: " + endpoint.getClass());
    }
}
