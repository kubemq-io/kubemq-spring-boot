package io.kubemq.spring.cloud.stream.binder.config;

import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter;
import io.kubemq.spring.cloud.stream.binder.KubeMQHeaderMapper;
import io.kubemq.spring.cloud.stream.binder.KubeMQMessageChannelBinder;
import io.kubemq.spring.cloud.stream.binder.properties.KubeMQBinderConfigurationProperties;
import io.kubemq.spring.cloud.stream.binder.properties.KubeMQExtendedBindingProperties;
import io.kubemq.spring.cloud.stream.binder.provisioning.KubeMQChannelProvisioner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the KubeMQ Spring Cloud Stream binder.
 *
 * <p>Creates the provisioner, header mapper, and binder beans. The {@link PubSubClient}
 * and {@link QueuesClient} are expected to be provided by the starter's auto-configuration,
 * enabling connection sharing between the starter and the binder.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(Binder.class)
@EnableConfigurationProperties({
        KubeMQBinderConfigurationProperties.class,
        KubeMQExtendedBindingProperties.class
})
public class KubeMQBinderConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KubeMQChannelProvisioner kubemqChannelProvisioner() {
        return new KubeMQChannelProvisioner();
    }

    @Bean
    @ConditionalOnMissingBean
    public KubeMQHeaderMapper kubemqHeaderMapper() {
        return new KubeMQHeaderMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public KubeMQMessageChannelBinder kubemqMessageChannelBinder(
            KubeMQChannelProvisioner provisioner,
            PubSubClient pubSubClient,
            QueuesClient queuesClient,
            KubeMQBinderConfigurationProperties binderProperties,
            KubeMQExtendedBindingProperties extendedProperties,
            KubeMQHeaderMapper headerMapper,
            ObjectProvider<KubeMQMessageConverter> converterProvider) {
        return new KubeMQMessageChannelBinder(
                new String[0],
                provisioner,
                pubSubClient,
                queuesClient,
                binderProperties,
                extendedProperties,
                headerMapper,
                converterProvider.getIfAvailable());
    }
}
