package io.kubemq.spring.cloud.stream.binder.properties;

/**
 * Per-binding producer properties for the KubeMQ Spring Cloud Stream binder.
 *
 * <p>Configured via {@code spring.cloud.stream.kubemq.bindings.<binding>.producer.*}.
 */
public class KubeMQProducerProperties {

    private KubeMQPattern pattern = KubeMQPattern.EVENTS;

    public KubeMQPattern getPattern() {
        return pattern;
    }

    public void setPattern(KubeMQPattern pattern) {
        this.pattern = pattern;
    }
}
