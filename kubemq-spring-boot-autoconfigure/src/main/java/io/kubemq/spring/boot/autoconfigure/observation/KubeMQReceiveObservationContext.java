package io.kubemq.spring.boot.autoconfigure.observation;

import io.micrometer.observation.Observation;

/**
 * Observation context carrying metadata for a KubeMQ <em>receive</em> operation.
 *
 * <p>Populated by the listener container before an observation is started,
 * and consumed by the {@link KubeMQObservationConvention} to produce
 * low-cardinality key values.
 */
public class KubeMQReceiveObservationContext extends Observation.Context {

    private final String channel;
    private final String pattern;

    /**
     * @param channel the source KubeMQ channel
     * @param pattern the messaging pattern (e.g. {@code "EVENTS"}, {@code "QUEUES"})
     */
    public KubeMQReceiveObservationContext(String channel, String pattern) {
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
