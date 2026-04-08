package io.kubemq.spring.cloud.stream.binder.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 * Combined binding properties holding both consumer and producer KubeMQ-specific settings.
 *
 * <p>Implements {@link BinderSpecificPropertiesProvider} so Spring Cloud Stream can inject
 * binder-specific properties per binding.
 */
public class KubeMQBindingProperties implements BinderSpecificPropertiesProvider {

    private KubeMQConsumerProperties consumer = new KubeMQConsumerProperties();

    private KubeMQProducerProperties producer = new KubeMQProducerProperties();

    @Override
    public KubeMQConsumerProperties getConsumer() {
        return consumer;
    }

    public void setConsumer(KubeMQConsumerProperties consumer) {
        this.consumer = consumer;
    }

    @Override
    public KubeMQProducerProperties getProducer() {
        return producer;
    }

    public void setProducer(KubeMQProducerProperties producer) {
        this.producer = producer;
    }
}
