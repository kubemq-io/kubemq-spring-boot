package io.kubemq.spring.cloud.stream.binder.properties;

import io.kubemq.sdk.pubsub.EventsStoreType;

/**
 * Per-binding consumer properties for the KubeMQ Spring Cloud Stream binder.
 *
 * <p>Configured via {@code spring.cloud.stream.kubemq.bindings.<binding>.consumer.*}.
 */
public class KubeMQConsumerProperties {

    private KubeMQPattern pattern = KubeMQPattern.EVENTS;

    private EventsStoreType eventsStoreType = EventsStoreType.StartNewOnly;

    private long eventsStoreSequenceValue = 0;

    private int pollMaxMessages = 1;

    private int pollWaitTimeoutInSeconds = 5;

    private int visibilitySeconds = 30;

    private boolean autoAckMessages = false;

    public KubeMQPattern getPattern() {
        return pattern;
    }

    public void setPattern(KubeMQPattern pattern) {
        this.pattern = pattern;
    }

    public EventsStoreType getEventsStoreType() {
        return eventsStoreType;
    }

    public void setEventsStoreType(EventsStoreType eventsStoreType) {
        this.eventsStoreType = eventsStoreType;
    }

    public long getEventsStoreSequenceValue() {
        return eventsStoreSequenceValue;
    }

    public void setEventsStoreSequenceValue(long eventsStoreSequenceValue) {
        this.eventsStoreSequenceValue = eventsStoreSequenceValue;
    }

    public int getPollMaxMessages() {
        return pollMaxMessages;
    }

    public void setPollMaxMessages(int pollMaxMessages) {
        this.pollMaxMessages = pollMaxMessages;
    }

    public int getPollWaitTimeoutInSeconds() {
        return pollWaitTimeoutInSeconds;
    }

    public void setPollWaitTimeoutInSeconds(int pollWaitTimeoutInSeconds) {
        this.pollWaitTimeoutInSeconds = pollWaitTimeoutInSeconds;
    }

    public int getVisibilitySeconds() {
        return visibilitySeconds;
    }

    public void setVisibilitySeconds(int visibilitySeconds) {
        this.visibilitySeconds = visibilitySeconds;
    }

    public boolean isAutoAckMessages() {
        return autoAckMessages;
    }

    public void setAutoAckMessages(boolean autoAckMessages) {
        this.autoAckMessages = autoAckMessages;
    }
}
