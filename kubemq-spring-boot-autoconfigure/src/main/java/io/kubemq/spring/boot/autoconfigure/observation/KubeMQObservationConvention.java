package io.kubemq.spring.boot.autoconfigure.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

/**
 * Convention applied to all KubeMQ observations (send and receive).
 *
 * <p>Implement this interface and register the implementation as a Spring bean
 * to customise observation names and key values. The
 * {@link DefaultKubeMQObservationConvention} is used when no custom bean is
 * present.
 *
 * @see DefaultKubeMQObservationConvention
 */
public interface KubeMQObservationConvention extends ObservationConvention<Observation.Context> {

    @Override
    default boolean supportsContext(Observation.Context context) {
        return context instanceof KubeMQSendObservationContext
                || context instanceof KubeMQReceiveObservationContext;
    }
}
