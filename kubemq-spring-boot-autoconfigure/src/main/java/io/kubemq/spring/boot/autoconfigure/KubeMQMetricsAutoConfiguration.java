package io.kubemq.spring.boot.autoconfigure;

import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for KubeMQ Micrometer metrics.
 *
 * <p>Registers the {@code kubemq.connection.state} gauge described in spec SS5.1.9.
 * Timer and counter meters for send/receive operations are created automatically
 * by the Observation API when {@link KubeMQObservationAutoConfiguration} is active.
 *
 * <p>The gauge probes all three clients (pubsub, queues, cq) and caches the result
 * for the configured scrape interval to avoid overwhelming the broker.
 */
@AutoConfiguration(after = KubeMQAutoConfiguration.class)
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnBean({PubSubClient.class, QueuesClient.class, CQClient.class})
@ConditionalOnProperty(prefix = "kubemq.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KubeMQMetricsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "kubemqConnectionStateMeterBinder")
    public MeterBinder kubemqConnectionStateMeterBinder(
            PubSubClient pubSubClient,
            QueuesClient queuesClient,
            CQClient cqClient,
            KubeMQProperties properties) {
        Duration scrapeInterval = properties.getMetrics().getScrapeInterval();
        return registry -> {
            AtomicReference<Double> cachedState = new AtomicReference<>(0.0);
            AtomicLong lastProbeTime = new AtomicLong(0);
            Gauge.builder("kubemq.connection.state", () -> {
                long now = System.currentTimeMillis();
                if (now - lastProbeTime.get() > scrapeInterval.toMillis()) {
                    lastProbeTime.set(now);
                    try {
                        pubSubClient.ping();
                        queuesClient.ping();
                        cqClient.ping();
                        cachedState.set(1.0);
                    } catch (Exception e) {
                        cachedState.set(0.0);
                    }
                }
                return cachedState.get();
            }).description("KubeMQ broker connection state (1=connected, 0=disconnected)")
            .register(registry);
        };
    }
}
