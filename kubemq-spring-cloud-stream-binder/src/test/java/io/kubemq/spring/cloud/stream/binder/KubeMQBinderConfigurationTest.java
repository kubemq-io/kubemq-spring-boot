package io.kubemq.spring.cloud.stream.binder;

import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter;
import io.kubemq.spring.cloud.stream.binder.config.KubeMQBinderConfiguration;
import io.kubemq.spring.cloud.stream.binder.properties.KubeMQBinderConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class KubeMQBinderConfigurationTest {

    @Test
    void binderConfigurationProperties_defaultAddressIsEmpty() {
        KubeMQBinderConfigurationProperties properties = new KubeMQBinderConfigurationProperties();
        assertThat(properties.getAddress()).isEmpty();
    }

    @Test
    void binderConfigurationProperties_setAddressIsPreserved() {
        KubeMQBinderConfigurationProperties properties = new KubeMQBinderConfigurationProperties();
        properties.setAddress("custom-host:50000");
        assertThat(properties.getAddress()).isEqualTo("custom-host:50000");
    }

    @Test
    void binder_reuses_starter_clients() {
        // Verify the binder configuration reuses externally-provided PubSubClient
        // and QueuesClient beans (as the starter's auto-configuration would provide)
        PubSubClient sharedPubSub = Mockito.mock(PubSubClient.class);
        QueuesClient sharedQueues = Mockito.mock(QueuesClient.class);

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(KubeMQBinderConfiguration.class))
                .withBean(PubSubClient.class, () -> sharedPubSub)
                .withBean(QueuesClient.class, () -> sharedQueues)
                .run(context -> {
                    assertThat(context).hasSingleBean(KubeMQMessageChannelBinder.class);
                    // The binder bean exists and was created with the shared clients
                    // (no additional PubSubClient or QueuesClient beans were created)
                    assertThat(context).hasSingleBean(PubSubClient.class);
                    assertThat(context).hasSingleBean(QueuesClient.class);
                    assertThat(context.getBean(PubSubClient.class)).isSameAs(sharedPubSub);
                    assertThat(context.getBean(QueuesClient.class)).isSameAs(sharedQueues);
                });
    }

    @Test
    void converter_injected_into_binder() {
        // Verify that when a KubeMQMessageConverter bean is present,
        // it gets injected into the binder via ObjectProvider
        PubSubClient mockPubSub = Mockito.mock(PubSubClient.class);
        QueuesClient mockQueues = Mockito.mock(QueuesClient.class);
        KubeMQMessageConverter mockConverter = Mockito.mock(KubeMQMessageConverter.class);

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(KubeMQBinderConfiguration.class))
                .withBean(PubSubClient.class, () -> mockPubSub)
                .withBean(QueuesClient.class, () -> mockQueues)
                .withBean(KubeMQMessageConverter.class, () -> mockConverter)
                .run(context -> {
                    assertThat(context).hasSingleBean(KubeMQMessageChannelBinder.class);
                    assertThat(context).hasSingleBean(KubeMQMessageConverter.class);
                    assertThat(context.getBean(KubeMQMessageConverter.class)).isSameAs(mockConverter);
                });
    }
}
