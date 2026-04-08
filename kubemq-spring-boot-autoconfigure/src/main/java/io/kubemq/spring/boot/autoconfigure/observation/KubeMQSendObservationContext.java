package io.kubemq.spring.boot.autoconfigure.observation;

import io.micrometer.observation.Observation;

/**
 * Observation context carrying metadata for a KubeMQ <em>send</em> operation.
 *
 * <p>Populated by {@link io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate}
 * before an observation is started, and consumed by the
 * {@link KubeMQObservationConvention} to produce low-cardinality key values.
 */
public class KubeMQSendObservationContext extends Observation.Context {

    private final String channel;
    private final String pattern;

    /**
     * @param channel the destination KubeMQ channel
     * @param pattern the messaging pattern (e.g. {@code "EVENTS"}, {@code "QUEUES"})
     */
    public KubeMQSendObservationContext(String channel, String pattern) {
        this.channel = channel;
        this.pattern = pattern;
    }

    public String getChannel() {
        return channel;
    }

    public String getPattern() {
        return pattern;
    }
}
