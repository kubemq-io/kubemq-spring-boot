package io.kubemq.spring.cloud.stream.binder;

import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter;
import io.kubemq.spring.cloud.stream.binder.adapter.KubeMQEventsMessageDrivenAdapter;
import io.kubemq.spring.cloud.stream.binder.adapter.KubeMQEventsStoreMessageDrivenAdapter;
import io.kubemq.spring.cloud.stream.binder.adapter.KubeMQQueuesMessageDrivenAdapter;
import io.kubemq.spring.cloud.stream.binder.handler.KubeMQEventsMessageHandler;
import io.kubemq.spring.cloud.stream.binder.handler.KubeMQEventsStoreMessageHandler;
import io.kubemq.spring.cloud.stream.binder.handler.KubeMQQueuesMessageHandler;
import io.kubemq.spring.cloud.stream.binder.properties.KubeMQBinderConfigurationProperties;
import io.kubemq.spring.cloud.stream.binder.properties.KubeMQBindingProperties;
import io.kubemq.spring.cloud.stream.binder.properties.KubeMQConsumerProperties;
import io.kubemq.spring.cloud.stream.binder.properties.KubeMQExtendedBindingProperties;
import io.kubemq.spring.cloud.stream.binder.properties.KubeMQPattern;
import io.kubemq.spring.cloud.stream.binder.properties.KubeMQProducerProperties;
import io.kubemq.spring.cloud.stream.binder.provisioning.KubeMQChannelProvisioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * Spring Cloud Stream binder for KubeMQ, supporting Events, Events Store, and Queues patterns.
 *
 * <p>Extends {@link AbstractMessageChannelBinder} to plug into the SCS infrastructure. Routes
 * producer/consumer creation to the correct KubeMQ handler/adapter based on
 * {@link KubeMQPattern} configured per binding.
 *
 * <p>Implements {@link ExtendedPropertiesBinder} to expose KubeMQ-specific consumer/producer
 * properties alongside the standard SCS properties.
 */
public class KubeMQMessageChannelBinder extends
        AbstractMessageChannelBinder<
                ExtendedConsumerProperties<KubeMQConsumerProperties>,
                ExtendedProducerProperties<KubeMQProducerProperties>,
                KubeMQChannelProvisioner>
        implements
        ExtendedPropertiesBinder<MessageChannel, KubeMQConsumerProperties, KubeMQProducerProperties> {

    private static final Logger log = LoggerFactory.getLogger(KubeMQMessageChannelBinder.class);

    private final PubSubClient pubSubClient;
    private final QueuesClient queuesClient;
    private final KubeMQBinderConfigurationProperties binderProperties;
    private final KubeMQExtendedBindingProperties extendedBindingProperties;
    private final KubeMQHeaderMapper headerMapper;
    private final KubeMQMessageConverter messageConverter;

    public KubeMQMessageChannelBinder(
            String[] headersToEmbed,
            KubeMQChannelProvisioner provisioningProvider,
            PubSubClient pubSubClient,
            QueuesClient queuesClient,
            KubeMQBinderConfigurationProperties binderProperties,
            KubeMQExtendedBindingProperties extendedBindingProperties,
            KubeMQHeaderMapper headerMapper,
            KubeMQMessageConverter messageConverter) {
        super(headersToEmbed, provisioningProvider);
        this.pubSubClient = pubSubClient;
        this.queuesClient = queuesClient;
        this.binderProperties = binderProperties;
        this.extendedBindingProperties = extendedBindingProperties;
        this.headerMapper = headerMapper;
        this.messageConverter = messageConverter;
    }

    @Override
    protected MessageHandler createProducerMessageHandler(
            ProducerDestination destination,
            ExtendedProducerProperties<KubeMQProducerProperties> producerProperties,
            MessageChannel errorChannel) throws Exception {
        KubeMQPattern pattern = producerProperties.getExtension().getPattern();
        String channel = destination.getName();
        log.info("Creating producer handler for channel '{}' with pattern {}", channel, pattern);

        return switch (pattern) {
            case EVENTS -> new KubeMQEventsMessageHandler(pubSubClient, channel, headerMapper, messageConverter);
            case EVENTS_STORE -> new KubeMQEventsStoreMessageHandler(pubSubClient, channel, headerMapper, messageConverter);
            case QUEUES -> new KubeMQQueuesMessageHandler(queuesClient, channel, headerMapper, messageConverter);
        };
    }

    @Override
    protected MessageProducer createConsumerEndpoint(
            ConsumerDestination destination,
            String group,
            ExtendedConsumerProperties<KubeMQConsumerProperties> properties) throws Exception {
        KubeMQConsumerProperties consumerProps = properties.getExtension();
        KubeMQPattern pattern = consumerProps.getPattern();
        String channel = destination.getName();
        String normalizedGroup = KubeMQBinderUtils.normalizeGroup(group);
        log.info("Creating consumer endpoint for channel '{}' with pattern {} (group='{}')",
                channel, pattern, normalizedGroup);

        return switch (pattern) {
            case EVENTS -> new KubeMQEventsMessageDrivenAdapter(
                    pubSubClient, channel, normalizedGroup, headerMapper);
            case EVENTS_STORE -> new KubeMQEventsStoreMessageDrivenAdapter(
                    pubSubClient, channel, normalizedGroup,
                    consumerProps.getEventsStoreType(),
                    consumerProps.getEventsStoreSequenceValue(),
                    headerMapper);
            case QUEUES -> new KubeMQQueuesMessageDrivenAdapter(
                    queuesClient, channel,
                    consumerProps.getPollMaxMessages(),
                    consumerProps.getPollWaitTimeoutInSeconds(),
                    consumerProps.getVisibilitySeconds(),
                    consumerProps.isAutoAckMessages(),
                    headerMapper);
        };
    }

    @Override
    public KubeMQConsumerProperties getExtendedConsumerProperties(String channelName) {
        return extendedBindingProperties.getExtendedConsumerProperties(channelName);
    }

    @Override
    public KubeMQProducerProperties getExtendedProducerProperties(String channelName) {
        return extendedBindingProperties.getExtendedProducerProperties(channelName);
    }

    @Override
    public String getDefaultsPrefix() {
        return extendedBindingProperties.getDefaultsPrefix();
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return KubeMQBindingProperties.class;
    }
}
