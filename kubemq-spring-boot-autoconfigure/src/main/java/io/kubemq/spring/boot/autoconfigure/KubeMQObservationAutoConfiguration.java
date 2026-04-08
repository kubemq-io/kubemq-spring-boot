package io.kubemq.spring.boot.autoconfigure;

import io.kubemq.spring.boot.autoconfigure.observation.DefaultKubeMQObservationConvention;
import io.kubemq.spring.boot.autoconfigure.observation.KubeMQObservationConvention;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for KubeMQ Micrometer Observation API integration.
 *
 * <p>Registers the {@link DefaultKubeMQObservationConvention} which provides
 * observation names and low-cardinality key values for both send and receive
 * paths. Users can override by defining a custom {@link KubeMQObservationConvention}
 * bean.
 */
@AutoConfiguration(after = {KubeMQTemplateAutoConfiguration.class, KubeMQListenerAutoConfiguration.class})
@ConditionalOnClass(ObservationRegistry.class)
@ConditionalOnBean(ObservationRegistry.class)
@ConditionalOnProperty(prefix = "kubemq.template", name = "observation-enabled", havingValue = "true", matchIfMissing = true)
public class KubeMQObservationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(KubeMQObservationConvention.class)
    public DefaultKubeMQObservationConvention defaultKubeMQObservationConvention() {
        return new DefaultKubeMQObservationConvention();
    }
}
