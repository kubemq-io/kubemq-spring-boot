package io.kubemq.spring.boot.autoconfigure.listener;

import java.lang.reflect.Method;

/**
 * Descriptor for a KubeMQ listener endpoint discovered from an annotated method.
 *
 * <p>Captures all metadata needed by {@link KubeMQListenerContainerFactory} to create
 * and configure a {@link KubeMQMessageListenerContainer}.
 */
public interface KubeMQListenerEndpoint {

    /**
     * Unique identifier for this endpoint.
     */
    String getId();

    /**
     * Messaging pattern type: EVENT, EVENT_STORE, QUEUE, COMMAND, QUERY.
     */
    KubeMQListenerType getType();

    /**
     * Channel(s) this endpoint listens on.
     */
    String[] getChannels();

    /**
     * Consumer group name (empty if none).
     */
    String getGroup();

    /**
     * Concurrency level. 0 or negative means use default from properties.
     */
    int getConcurrency();

    /**
     * Whether this endpoint auto-starts.
     */
    boolean isAutoStartup();

    /**
     * The bean instance containing the listener method.
     */
    Object getBean();

    /**
     * The listener method to invoke.
     */
    Method getMethod();

    /**
     * Bean name of a custom error handler (empty if default).
     */
    String getErrorHandlerBeanName();

    /**
     * Bean name of a custom container factory (empty if default).
     */
    String getContainerFactoryBeanName();

    /**
     * Enumeration of the five KubeMQ listener types.
     */
    enum KubeMQListenerType {
        EVENT,
        EVENT_STORE,
        QUEUE,
        COMMAND,
        QUERY
    }
}
