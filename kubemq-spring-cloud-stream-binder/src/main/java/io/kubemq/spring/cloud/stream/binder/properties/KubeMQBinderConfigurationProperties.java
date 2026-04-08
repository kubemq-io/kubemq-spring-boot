package io.kubemq.spring.cloud.stream.binder.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binder-level configuration properties for the KubeMQ Spring Cloud Stream binder.
 *
 * <p>Configured via {@code spring.cloud.stream.kubemq.binder.*}. Falls back to the
 * starter's {@code kubemq.*} properties if not explicitly set.
 */
@ConfigurationProperties(prefix = "spring.cloud.stream.kubemq.binder")
public class KubeMQBinderConfigurationProperties {

    /**
     * Optional address override for the binder. When empty, falls back to
     * the starter's {@code kubemq.address} property.
     */
    private String address = "";

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
