package io.kubemq.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Tests for {@link KubeMQProperties} binding.
 */
class KubeMQPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KubeMQAutoConfiguration.class))
            .withBean(ObjectMapper.class, ObjectMapper::new);

    @Test
    void backoff_properties_bind_correctly() {
        contextRunner
                .withPropertyValues(
                        "kubemq.address=localhost:50000",
                        "kubemq.client-id=test",
                        "kubemq.listener.queues.error-backoff-initial=2s",
                        "kubemq.listener.queues.error-backoff-max=60s",
                        "kubemq.listener.queues.error-backoff-multiplier=3.0")
                .run(context -> {
                    KubeMQProperties props = context.getBean(KubeMQProperties.class);
                    assertThat(props.getListener().getQueues().getErrorBackoffInitial())
                            .isEqualTo(Duration.ofSeconds(2));
                    assertThat(props.getListener().getQueues().getErrorBackoffMax())
                            .isEqualTo(Duration.ofSeconds(60));
                    assertThat(props.getListener().getQueues().getErrorBackoffMultiplier())
                            .isEqualTo(3.0);
                });
    }

    @Test
    void health_properties_bind_correctly() {
        contextRunner
                .withPropertyValues(
                        "kubemq.address=localhost:50000",
                        "kubemq.client-id=test",
                        "kubemq.health.enabled=true",
                        "kubemq.health.timeout=10s",
                        "kubemq.health.cache-duration=30s")
                .run(context -> {
                    KubeMQProperties props = context.getBean(KubeMQProperties.class);
                    assertThat(props.getHealth().isEnabled()).isTrue();
                    assertThat(props.getHealth().getTimeout()).isEqualTo(Duration.ofSeconds(10));
                    assertThat(props.getHealth().getCacheDuration()).isEqualTo(Duration.ofSeconds(30));
                });
    }

    @Test
    void kotlin_dispatcher_property_binds_correctly() {
        contextRunner
                .withPropertyValues(
                        "kubemq.address=localhost:50000",
                        "kubemq.client-id=test",
                        "kubemq.kotlin.dispatcher=io")
                .run(context -> {
                    KubeMQProperties props = context.getBean(KubeMQProperties.class);
                    assertThat(props.getKotlin().getDispatcher()).isEqualTo("io");
                });
    }

    @Test
    void default_values_are_sensible() {
        KubeMQProperties props = new KubeMQProperties();

        assertThat(props.isEnabled()).isTrue();
        assertThat(props.getAddress()).isEqualTo("localhost:50000");
        assertThat(props.getClientId()).isEmpty();
        assertThat(props.getAuthToken()).isEmpty();
        assertThat(props.getTls().isEnabled()).isFalse();
        assertThat(props.getConnection().getTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(props.getListener().getConcurrency()).isEqualTo(1);
        assertThat(props.getListener().isAutoStartup()).isTrue();
        assertThat(props.getListener().getShutdownTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(props.getListener().getQueues().getPollTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(props.getListener().getQueues().getMaxPollMessages()).isEqualTo(1);
        assertThat(props.getListener().getQueues().isAutoAck()).isFalse();
        assertThat(props.getTemplate().isObservationEnabled()).isTrue();
        assertThat(props.getHealth().isEnabled()).isTrue();
        assertThat(props.getHealth().getTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(props.getHealth().getCacheDuration()).isEqualTo(Duration.ofSeconds(15));
        assertThat(props.getKotlin().getDispatcher()).isEqualTo("default");
    }
}
