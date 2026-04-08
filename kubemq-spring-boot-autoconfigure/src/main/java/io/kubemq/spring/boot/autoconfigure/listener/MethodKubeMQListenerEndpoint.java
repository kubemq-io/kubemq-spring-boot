package io.kubemq.spring.boot.autoconfigure.listener;

import io.kubemq.sdk.pubsub.EventsStoreType;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Concrete {@link KubeMQListenerEndpoint} backed by a bean method annotated with one
 * of the five KubeMQ listener annotations.
 */
public class MethodKubeMQListenerEndpoint implements KubeMQListenerEndpoint {

    private final String id;
    private final KubeMQListenerType type;
    private final String[] channels;
    private final String group;
    private final int concurrency;
    private final boolean autoStartup;
    private final Object bean;
    private final Method method;
    private final String errorHandlerBeanName;
    private final String containerFactoryBeanName;

    // Event-store specific
    private final EventsStoreType eventsStoreType;
    private final long eventsStoreValue;

    // Queue specific
    private final int pollTimeoutSeconds;
    private final int maxPollMessages;
    private final int visibilityTimeoutSeconds;
    private final boolean autoAck;
    private final boolean batch;

    private MethodKubeMQListenerEndpoint(Builder builder) {
        this.id = Objects.requireNonNull(builder.id);
        this.type = Objects.requireNonNull(builder.type);
        this.channels = Objects.requireNonNull(builder.channels);
        this.group = builder.group != null ? builder.group : "";
        this.concurrency = builder.concurrency;
        this.autoStartup = builder.autoStartup;
        this.bean = Objects.requireNonNull(builder.bean);
        this.method = Objects.requireNonNull(builder.method);
        this.errorHandlerBeanName = builder.errorHandlerBeanName != null ? builder.errorHandlerBeanName : "";
        this.containerFactoryBeanName = builder.containerFactoryBeanName != null ? builder.containerFactoryBeanName : "";
        this.eventsStoreType = builder.eventsStoreType;
        this.eventsStoreValue = builder.eventsStoreValue;
        this.pollTimeoutSeconds = builder.pollTimeoutSeconds;
        this.maxPollMessages = builder.maxPollMessages;
        this.visibilityTimeoutSeconds = builder.visibilityTimeoutSeconds;
        this.autoAck = builder.autoAck;
        this.batch = builder.batch;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public KubeMQListenerType getType() {
        return type;
    }

    @Override
    public String[] getChannels() {
        return channels;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public int getConcurrency() {
        return concurrency;
    }

    @Override
    public boolean isAutoStartup() {
        return autoStartup;
    }

    @Override
    public Object getBean() {
        return bean;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public String getErrorHandlerBeanName() {
        return errorHandlerBeanName;
    }

    @Override
    public String getContainerFactoryBeanName() {
        return containerFactoryBeanName;
    }

    public EventsStoreType getEventsStoreType() {
        return eventsStoreType;
    }

    public long getEventsStoreValue() {
        return eventsStoreValue;
    }

    public int getPollTimeoutSeconds() {
        return pollTimeoutSeconds;
    }

    public int getMaxPollMessages() {
        return maxPollMessages;
    }

    public int getVisibilityTimeoutSeconds() {
        return visibilityTimeoutSeconds;
    }

    public boolean isAutoAck() {
        return autoAck;
    }

    public boolean isBatch() {
        return batch;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private KubeMQListenerType type;
        private String[] channels;
        private String group;
        private int concurrency;
        private boolean autoStartup = true;
        private Object bean;
        private Method method;
        private String errorHandlerBeanName;
        private String containerFactoryBeanName;
        private EventsStoreType eventsStoreType;
        private long eventsStoreValue;
        private int pollTimeoutSeconds;
        private int maxPollMessages = 1;
        private int visibilityTimeoutSeconds;
        private boolean autoAck;
        private boolean batch;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder type(KubeMQListenerType type) {
            this.type = type;
            return this;
        }

        public Builder channels(String[] channels) {
            this.channels = channels;
            return this;
        }

        public Builder group(String group) {
            this.group = group;
            return this;
        }

        public Builder concurrency(int concurrency) {
            this.concurrency = concurrency;
            return this;
        }

        public Builder autoStartup(boolean autoStartup) {
            this.autoStartup = autoStartup;
            return this;
        }

        public Builder bean(Object bean) {
            this.bean = bean;
            return this;
        }

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder errorHandlerBeanName(String errorHandlerBeanName) {
            this.errorHandlerBeanName = errorHandlerBeanName;
            return this;
        }

        public Builder containerFactoryBeanName(String containerFactoryBeanName) {
            this.containerFactoryBeanName = containerFactoryBeanName;
            return this;
        }

        public Builder eventsStoreType(EventsStoreType eventsStoreType) {
            this.eventsStoreType = eventsStoreType;
            return this;
        }

        public Builder eventsStoreValue(long eventsStoreValue) {
            this.eventsStoreValue = eventsStoreValue;
            return this;
        }

        public Builder pollTimeoutSeconds(int pollTimeoutSeconds) {
            this.pollTimeoutSeconds = pollTimeoutSeconds;
            return this;
        }

        public Builder maxPollMessages(int maxPollMessages) {
            this.maxPollMessages = maxPollMessages;
            return this;
        }

        public Builder visibilityTimeoutSeconds(int visibilityTimeoutSeconds) {
            this.visibilityTimeoutSeconds = visibilityTimeoutSeconds;
            return this;
        }

        public Builder autoAck(boolean autoAck) {
            this.autoAck = autoAck;
            return this;
        }

        public Builder batch(boolean batch) {
            this.batch = batch;
            return this;
        }

        public MethodKubeMQListenerEndpoint build() {
            return new MethodKubeMQListenerEndpoint(this);
        }
    }
}
