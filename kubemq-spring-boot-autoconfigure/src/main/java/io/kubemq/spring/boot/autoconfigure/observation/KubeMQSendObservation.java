package io.kubemq.spring.boot.autoconfigure.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * {@link ObservationDocumentation} for KubeMQ <em>send</em> operations.
 *
 * <p>Creates a timer named {@code kubemq.send} with low-cardinality tags
 * {@code kubemq.channel} and {@code kubemq.pattern}.
 */
public enum KubeMQSendObservation implements ObservationDocumentation {

    /** Observation recorded around every template send call. */
    SEND {
        @Override
        public String getName() {
            return "kubemq.send";
        }

        @Override
        public KeyName[] getLowCardinalityKeyNames() {
            return SendLowCardinalityKeyNames.values();
        }

        @Override
        public KeyName[] getHighCardinalityKeyNames() {
            return SendHighCardinalityKeyNames.values();
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return DefaultKubeMQObservationConvention.class;
        }
    };

    /** Low-cardinality key names attached to every send observation. */
    public enum SendLowCardinalityKeyNames implements KeyName {

        /** The messaging pattern ({@code EVENTS}, {@code EVENTS_STORE}, {@code QUEUES}, {@code COMMANDS}, {@code QUERIES}). */
        PATTERN {
            @Override
            public String asString() {
                return "kubemq.pattern";
            }
        }
    }

    /** High-cardinality key names attached to every send observation. */
    public enum SendHighCardinalityKeyNames implements KeyName {

        /** The destination KubeMQ channel name (unbounded). */
        CHANNEL {
            @Override
            public String asString() {
                return "kubemq.channel";
            }
        }
    }
}
