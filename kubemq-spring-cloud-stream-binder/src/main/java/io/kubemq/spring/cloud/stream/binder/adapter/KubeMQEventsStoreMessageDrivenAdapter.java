package io.kubemq.spring.cloud.stream.binder.adapter;

import io.kubemq.sdk.pubsub.EventStoreMessageReceived;
import io.kubemq.sdk.pubsub.EventsStoreSubscription;
import io.kubemq.sdk.pubsub.EventsStoreType;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.spring.cloud.stream.binder.KubeMQHeaderMapper;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Spring Cloud Stream consumer adapter for KubeMQ Events Store (persistent pub/sub).
 *
 * <p>Subscribes to a KubeMQ events store channel via
 * {@link PubSubClient#subscribeToEventsStore} and forwards received messages
 * to the Spring Cloud Stream output channel.
 */
public class KubeMQEventsStoreMessageDrivenAdapter extends MessageProducerSupport {

    private static final Logger log = LoggerFactory.getLogger(KubeMQEventsStoreMessageDrivenAdapter.class);

    private final PubSubClient pubSubClient;
    private final String channel;
    private final String group;
    private final EventsStoreType eventsStoreType;
    private final long eventsStoreSequenceValue;
    private final KubeMQHeaderMapper headerMapper;
    private volatile EventsStoreSubscription subscription;

    public KubeMQEventsStoreMessageDrivenAdapter(PubSubClient pubSubClient, String channel,
                                                  String group, EventsStoreType eventsStoreType,
                                                  long eventsStoreSequenceValue,
                                                  KubeMQHeaderMapper headerMapper) {
        this.pubSubClient = pubSubClient;
        this.channel = channel;
        this.group = group != null ? group : "";
        this.eventsStoreType = eventsStoreType;
        this.eventsStoreSequenceValue = eventsStoreSequenceValue;
        this.headerMapper = headerMapper;
    }

    @Override
    protected void doStart() {
        log.info("Starting events store subscription on channel '{}' (group='{}', storeType={})",
                channel, group, eventsStoreType);
        subscription = EventsStoreSubscription.builder()
                .channel(channel)
                .group(group)
                .eventsStoreType(eventsStoreType)
                .eventsStoreSequenceValue(eventsStoreSequenceValue)
                .onReceiveEventCallback(this::onMessage)
                .onErrorCallback(error ->
                        log.error("Events store subscription error on channel '{}': {}",
                                channel, error.getMessage(), error))
                .build();
        pubSubClient.subscribeToEventsStore(subscription);
    }

    @Override
    protected void doStop() {
        log.info("Stopping events store subscription on channel '{}'", channel);
        EventsStoreSubscription sub = this.subscription;
        if (sub != null) {
            sub.cancel();
            this.subscription = null;
        }
    }

    private void onMessage(EventStoreMessageReceived received) {
        Map<String, String> tags = received.getTags();
        MessageHeaders headers = headerMapper.toSpringHeaders(tags);
        Message<byte[]> message = MessageBuilder
                .withPayload(received.getBody() != null ? received.getBody() : new byte[0])
                .copyHeaders(headers)
                .setHeader("kubemq_channel", received.getChannel())
                .setHeader("kubemq_id", received.getId())
                .setHeader("kubemq_metadata", received.getMetadata())
                .setHeader("kubemq_sequence", received.getSequence())
                .build();
        sendMessage(message);
    }
}
