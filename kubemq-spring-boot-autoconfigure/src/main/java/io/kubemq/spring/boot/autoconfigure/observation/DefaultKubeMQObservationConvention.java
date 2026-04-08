package io.kubemq.spring.boot.autoconfigure.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;

/**
 * Default {@link KubeMQObservationConvention} implementation that produces
 * contextual names and low-cardinality key values for both send and receive
 * observations.
 *
 * <p>Key values emitted:
 * <ul>
 *   <li>{@code kubemq.channel} — destination/source channel name</li>
 *   <li>{@code kubemq.pattern} — messaging pattern (EVENTS, EVENTS_STORE, QUEUES, COMMANDS, QUERIES)</li>
 * </ul>
 */
public class DefaultKubeMQObservationConvention implements KubeMQObservationConvention {

    static final String TAG_CHANNEL = "kubemq.channel";
    static final String TAG_PATTERN = "kubemq.pattern";

    @Override
    public String getName() {
        return "kubemq";
    }

    @Override
    public String getContextualName(Observation.Context context) {
        if (context instanceof KubeMQSendObservationContext ctx) {
            return "kubemq " + ctx.getPattern().toLowerCase() + " send";
        }
        if (context instanceof KubeMQReceiveObservationContext ctx) {
            return "kubemq " + ctx.getPattern().toLowerCase() + " receive";
        }
        return "kubemq";
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(Observation.Context context) {
        // Only pattern (EVENTS, EVENTS_STORE, QUEUES, COMMANDS, QUERIES) -- bounded set
        if (context instanceof KubeMQSendObservationContext ctx) {
            return KeyValues.of(KeyValue.of(TAG_PATTERN, ctx.getPattern()));
        }
        if (context instanceof KubeMQReceiveObservationContext ctx) {
            return KeyValues.of(KeyValue.of(TAG_PATTERN, ctx.getPattern()));
        }
        return KeyValues.empty();
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(Observation.Context context) {
        // Channel name is high cardinality -- unbounded
        if (context instanceof KubeMQSendObservationContext ctx) {
            return KeyValues.of(KeyValue.of(TAG_CHANNEL, ctx.getChannel()));
        }
        if (context instanceof KubeMQReceiveObservationContext ctx) {
            return KeyValues.of(KeyValue.of(TAG_CHANNEL, ctx.getChannel()));
        }
        return KeyValues.empty();
    }
}
