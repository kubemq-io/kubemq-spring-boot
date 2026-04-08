package io.kubemq.spring.boot.autoconfigure;

import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerAnnotationBeanPostProcessor;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerContainerFactory;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerEndpointRegistrar;
import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import io.kubemq.spring.boot.autoconfigure.support.KubeMQCoroutineBridge;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ErrorHandler;

/**
 * Auto-configuration for the KubeMQ listener infrastructure:
 * annotation scanning, endpoint registration, and container factory.
 */
@AutoConfiguration(after = KubeMQAutoConfiguration.class)
@ConditionalOnBean(PubSubClient.class)
public class KubeMQListenerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KubeMQListenerAnnotationBeanPostProcessor kubemqListenerAnnotationProcessor(
            KubeMQListenerEndpointRegistrar registrar) {
        return new KubeMQListenerAnnotationBeanPostProcessor(registrar);
    }

    @Bean
    @ConditionalOnMissingBean
    public KubeMQListenerEndpointRegistrar kubemqListenerEndpointRegistrar(
            KubeMQListenerContainerFactory containerFactory,
            ConfigurableListableBeanFactory beanFactory) {
        return new KubeMQListenerEndpointRegistrar(containerFactory, beanFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public KubeMQListenerContainerFactory kubemqListenerContainerFactory(
            PubSubClient pubSubClient,
            QueuesClient queuesClient,
            CQClient cqClient,
            KubeMQProperties properties,
            ObjectProvider<ErrorHandler> errorHandler,
            ObjectProvider<KubeMQMessageConverter> messageConverter,
            ObjectProvider<KubeMQCoroutineBridge> coroutineSupportProvider) {
        return new KubeMQListenerContainerFactory(
                pubSubClient,
                queuesClient,
                cqClient,
                properties,
                errorHandler.getIfAvailable(),
                messageConverter.getIfAvailable(),
                coroutineSupportProvider.getIfAvailable());
    }
}
