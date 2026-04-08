package io.kubemq.spring.cloud.stream.binder.provisioning;

import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;

/**
 * Represents a KubeMQ channel destination for Spring Cloud Stream.
 *
 * <p>KubeMQ channels are auto-created on first use, so this is a simple name holder
 * implementing both {@link ProducerDestination} and {@link ConsumerDestination}.
 */
public class KubeMQDestination implements ProducerDestination, ConsumerDestination {

    private final String name;

    public KubeMQDestination(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNameForPartition(int partition) {
        return name;
    }

    @Override
    public String toString() {
        return "KubeMQDestination{name='" + name + "'}";
    }
}
