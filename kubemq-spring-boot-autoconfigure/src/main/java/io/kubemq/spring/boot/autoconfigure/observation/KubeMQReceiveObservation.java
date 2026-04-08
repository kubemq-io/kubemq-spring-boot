package io.kubemq.spring.boot.autoconfigure.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * {@link ObservationDocumentation} for KubeMQ <em>receive</em> operations.
 *
 * <p>Creates a timer named {@code kubemq.receive} with low-cardinality tags
 * {@code kubemq.channel} and {@code kubemq.pattern}.
 */
public enum KubeMQReceiveObservation implements ObservationDocumentation {

    /** Observation recorded around every listener message delivery. */
    RECEIVE {
        @Override
        public String getName() {
            return "kubemq.receive";
        }

        @Override
        public KeyName[] getLowCardinalityKeyNames() {
            return ReceiveLowCardinalityKeyNames.values();
        }

        @Override
        public KeyName[] getHighCardinalityKeyNames() {
            return ReceiveHighCardinalityKeyNames.values();
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return DefaultKubeMQObservationConvention.class;
        }
    };

    /** Low-cardinality key names attached to every receive observation. */
    public enum ReceiveLowCardinalityKeyNames implements KeyName {

        /** The messaging pattern ({@code EVENTS}, {@code EVENTS_STORE}, {@code QUEUES}, {@code COMMANDS}, {@code QUERIES}). */
        PATTERN {
            @Override
            public String asString() {
                return "kubemq.pattern";
            }
        }
    }

    /** High-cardinality key names attached to every receive observation. */
    public enum ReceiveHighCardinalityKeyNames implements KeyName {

        /** The source KubeMQ channel name (unbounded). */
        CHANNEL {
            @Override
            public String asString() {
                return "kubemq.channel";
            }
        }
    }
}
