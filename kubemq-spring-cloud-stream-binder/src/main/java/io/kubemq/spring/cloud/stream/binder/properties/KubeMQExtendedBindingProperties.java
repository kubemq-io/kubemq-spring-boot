package io.kubemq.spring.cloud.stream.binder.properties;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.AbstractExtendedBindingProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 * Extended binding properties for the KubeMQ binder.
 *
 * <p>Maps per-binding consumer/producer properties from
 * {@code spring.cloud.stream.kubemq.bindings.<binding>.*}.
 */
@ConfigurationProperties(prefix = "spring.cloud.stream.kubemq")
public class KubeMQExtendedBindingProperties
        extends AbstractExtendedBindingProperties<KubeMQConsumerProperties,
                KubeMQProducerProperties, KubeMQBindingProperties> {

    private static final String DEFAULTS_PREFIX = "spring.cloud.stream.kubemq.default";

    private Map<String, KubeMQBindingProperties> bindings = new HashMap<>();

    @Override
    public Map<String, KubeMQBindingProperties> getBindings() {
        return bindings;
    }

    @Override
    public void setBindings(Map<String, KubeMQBindingProperties> bindings) {
        this.bindings = bindings;
    }

    @Override
    public String getDefaultsPrefix() {
        return DEFAULTS_PREFIX;
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return KubeMQBindingProperties.class;
    }
}
