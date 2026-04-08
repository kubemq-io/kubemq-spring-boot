package io.kubemq.spring.cloud.stream.binder.properties;

/**
 * KubeMQ messaging pattern used for routing within the Spring Cloud Stream binder.
 */
public enum KubeMQPattern {
    EVENTS,
    EVENTS_STORE,
    QUEUES
}
