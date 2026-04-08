package io.kubemq.spring.cloud.stream.binder.provisioning;

import io.kubemq.spring.cloud.stream.binder.properties.KubeMQConsumerProperties;
import io.kubemq.spring.cloud.stream.binder.properties.KubeMQProducerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

/**
 * No-op provisioner for KubeMQ. KubeMQ channels are automatically created on first use,
 * so provisioning simply returns a {@link KubeMQDestination} wrapping the channel name.
 */
public class KubeMQChannelProvisioner implements ProvisioningProvider<
        ExtendedConsumerProperties<KubeMQConsumerProperties>,
        ExtendedProducerProperties<KubeMQProducerProperties>> {

    private static final Logger log = LoggerFactory.getLogger(KubeMQChannelProvisioner.class);

    @Override
    public ProducerDestination provisionProducerDestination(
            String name,
            ExtendedProducerProperties<KubeMQProducerProperties> properties) {
        log.debug("Provisioning producer destination: {}", name);
        return new KubeMQDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(
            String name,
            String group,
            ExtendedConsumerProperties<KubeMQConsumerProperties> properties) {
        log.debug("Provisioning consumer destination: {} (group={})", name, group);
        return new KubeMQDestination(name);
    }
}
