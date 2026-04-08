package io.kubemq.spring.boot.autoconfigure;

import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.actuator.KubeMQEndpoint;
import io.kubemq.spring.boot.autoconfigure.health.KubeMQHealthIndicator;
import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for the KubeMQ health indicator and actuator endpoint.
 */
@AutoConfiguration(after = KubeMQAutoConfiguration.class)
@ConditionalOnClass(PubSubClient.class)
@ConditionalOnBean({PubSubClient.class, QueuesClient.class, CQClient.class})
public class KubeMQHealthContributorAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(HealthIndicator.class)
    @ConditionalOnEnabledHealthIndicator("kubemq")
    static class KubeMQHealthIndicatorConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "kubemqHealthIndicator")
        public KubeMQHealthIndicator kubemqHealthIndicator(
                PubSubClient pubSubClient,
                QueuesClient queuesClient,
                CQClient cqClient,
                KubeMQProperties properties) {
            return new KubeMQHealthIndicator(
                    pubSubClient, queuesClient, cqClient,
                    properties.getHealth().getTimeout(),
                    properties.getHealth().getCacheDuration());
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Endpoint.class)
    static class KubeMQActuatorEndpointConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public KubeMQEndpoint kubemqEndpoint(
                PubSubClient pubSubClient,
                QueuesClient queuesClient,
                CQClient cqClient) {
            return new KubeMQEndpoint(pubSubClient, queuesClient, cqClient);
        }
    }
}
