package io.kubemq.spring.boot.autoconfigure;

import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter;
import io.kubemq.spring.boot.autoconfigure.observation.KubeMQObservationConvention;
import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for {@link KubeMQTemplate}.
 * Loads after {@link KubeMQAutoConfiguration} so that client beans are available.
 */
@AutoConfiguration(after = KubeMQAutoConfiguration.class)
@ConditionalOnBean(PubSubClient.class)
public class KubeMQTemplateAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KubeMQTemplate kubemqTemplate(
            PubSubClient pubSubClient,
            QueuesClient queuesClient,
            CQClient cqClient,
            KubeMQProperties properties,
            ObjectProvider<KubeMQMessageConverter> messageConverter,
            ObjectProvider<ObservationRegistry> observationRegistry,
            ObjectProvider<KubeMQObservationConvention> observationConvention) {
        return new KubeMQTemplate(
                pubSubClient,
                queuesClient,
                cqClient,
                messageConverter.getIfAvailable(),
                observationRegistry.getIfAvailable(),
                observationConvention.getIfAvailable(),
                properties);
    }
}
