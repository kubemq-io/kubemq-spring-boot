package io.kubemq.spring.boot.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannel;
import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.converter.JacksonKubeMQMessageConverter;
import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter;
import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import java.time.Duration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * Core auto-configuration that creates KubeMQ SDK client beans
 * and the default {@link KubeMQMessageConverter}.
 */
@AutoConfiguration
@ConditionalOnClass(PubSubClient.class)
@ConditionalOnProperty(prefix = "kubemq", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(KubeMQProperties.class)
public class KubeMQAutoConfiguration {

    /**
     * Deduplicated SDK client configuration extracted from {@link KubeMQProperties}.
     * Eliminates repetitive builder calls across all three client bean methods.
     */
    private record SdkClientConfig(
            String address, String clientId, String authToken,
            boolean tls, String certFile, String keyFile, String caCertFile,
            int keepAliveIntervalSeconds, int keepAliveTimeoutSeconds,
            int maxReceiveSize, int connectionTimeoutSeconds) {

        static SdkClientConfig from(KubeMQProperties props) {
            return new SdkClientConfig(
                    props.getAddress(),
                    props.getClientId(),
                    emptyToNull(props.getAuthToken()),
                    props.getTls().isEnabled(),
                    emptyToNull(props.getTls().getCertFile()),
                    emptyToNull(props.getTls().getKeyFile()),
                    emptyToNull(props.getTls().getCaCertFile()),
                    toSecondsAtLeast1(props.getConnection().getKeepAlive().getTime()),
                    toSecondsAtLeast1(props.getConnection().getKeepAlive().getTimeout()),
                    (int) props.getConnection().getMaxReceiveSize().toBytes(),
                    toSecondsAtLeast1(props.getConnection().getTimeout()));
        }
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public PubSubClient kubemqPubSubClient(KubeMQProperties properties,
            ObjectProvider<ManagedChannel> testChannel) {
        warnInsecureAuthToken(properties);
        ManagedChannel channel = testChannel.getIfAvailable();
        if (channel != null) {
            // Test mode: use pre-built InProcess channel (e.g. from MockKubeMQServer)
            return PubSubClient.builder()
                    .address("in-process")
                    .clientId(properties.getClientId())
                    .build();
        }
        SdkClientConfig cfg = SdkClientConfig.from(properties);
        return PubSubClient.builder()
                .address(cfg.address())
                .clientId(cfg.clientId())
                .authToken(cfg.authToken())
                .tls(cfg.tls())
                .tlsCertFile(cfg.certFile())
                .tlsKeyFile(cfg.keyFile())
                .caCertFile(cfg.caCertFile())
                .keepAlive(true)
                .pingIntervalInSeconds(cfg.keepAliveIntervalSeconds())
                .pingTimeoutInSeconds(cfg.keepAliveTimeoutSeconds())
                .maxReceiveSize(cfg.maxReceiveSize())
                .build();
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public QueuesClient kubemqQueuesClient(KubeMQProperties properties,
            ObjectProvider<ManagedChannel> testChannel) {
        warnInsecureAuthToken(properties);
        ManagedChannel channel = testChannel.getIfAvailable();
        if (channel != null) {
            // Test mode: use pre-built InProcess channel
            return QueuesClient.builder()
                    .address("in-process")
                    .clientId(properties.getClientId())
                    .build();
        }
        SdkClientConfig cfg = SdkClientConfig.from(properties);
        return QueuesClient.builder()
                .address(cfg.address())
                .clientId(cfg.clientId())
                .authToken(cfg.authToken())
                .tls(cfg.tls())
                .tlsCertFile(cfg.certFile())
                .tlsKeyFile(cfg.keyFile())
                .caCertFile(cfg.caCertFile())
                .keepAlive(true)
                .pingIntervalInSeconds(cfg.keepAliveIntervalSeconds())
                .pingTimeoutInSeconds(cfg.keepAliveTimeoutSeconds())
                .maxReceiveSize(cfg.maxReceiveSize())
                .build();
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public CQClient kubemqCQClient(KubeMQProperties properties,
            ObjectProvider<ManagedChannel> testChannel) {
        warnInsecureAuthToken(properties);
        ManagedChannel channel = testChannel.getIfAvailable();
        if (channel != null) {
            // Test mode: use pre-built InProcess channel
            return CQClient.builder()
                    .address("in-process")
                    .clientId(properties.getClientId())
                    .build();
        }
        SdkClientConfig cfg = SdkClientConfig.from(properties);
        return CQClient.builder()
                .address(cfg.address())
                .clientId(cfg.clientId())
                .authToken(cfg.authToken())
                .tls(cfg.tls())
                .tlsCertFile(cfg.certFile())
                .tlsKeyFile(cfg.keyFile())
                .caCertFile(cfg.caCertFile())
                .keepAlive(true)
                .pingIntervalInSeconds(cfg.keepAliveIntervalSeconds())
                .pingTimeoutInSeconds(cfg.keepAliveTimeoutSeconds())
                .maxReceiveSize(cfg.maxReceiveSize())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(ObjectMapper.class)
    public KubeMQMessageConverter kubemqMessageConverter(ObjectMapper objectMapper) {
        return new JacksonKubeMQMessageConverter(objectMapper);
    }

    // ==================== Internal Helpers ====================

    /**
     * Returns null for null or empty strings (used to avoid sending empty
     * TLS file paths to the SDK builder).
     */
    private static String emptyToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }

    /**
     * Converts a {@link Duration} to whole seconds, rounding up sub-second
     * values so that e.g. 1500ms becomes 2 rather than being truncated to 1.
     * Returns 0 for null/zero durations.
     */
    static int toSecondsAtLeast1(Duration duration) {
        if (duration == null || duration.isZero()) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(duration.toMillis() / 1000.0));
    }

    /**
     * Logs a warning when an auth token is configured but TLS is disabled,
     * meaning the token will be sent in plaintext over the wire.
     */
    private static void warnInsecureAuthToken(KubeMQProperties properties) {
        if (StringUtils.hasText(properties.getAuthToken()) && !properties.getTls().isEnabled()) {
            LoggerFactory.getLogger(KubeMQAutoConfiguration.class)
                    .warn("KubeMQ authToken is configured but TLS is disabled. "
                            + "The token will be sent in plaintext. "
                            + "Set kubemq.tls.enabled=true for secure token transport.");
        }
    }
}
