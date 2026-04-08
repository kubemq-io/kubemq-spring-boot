package io.kubemq.spring.cloud.stream.binder.adapter;

import io.kubemq.sdk.pubsub.EventMessageReceived;
import io.kubemq.sdk.pubsub.EventsSubscription;
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
 * Spring Cloud Stream consumer adapter for KubeMQ Events (fire-and-forget pub/sub).
 *
 * <p>Subscribes to a KubeMQ events channel via {@link PubSubClient#subscribeToEvents}
 * and forwards received messages to the Spring Cloud Stream output channel.
 */
public class KubeMQEventsMessageDrivenAdapter extends MessageProducerSupport {

    private static final Logger log = LoggerFactory.getLogger(KubeMQEventsMessageDrivenAdapter.class);

    private final PubSubClient pubSubClient;
    private final String channel;
    private final String group;
    private final KubeMQHeaderMapper headerMapper;
    private volatile EventsSubscription subscription;

    public KubeMQEventsMessageDrivenAdapter(PubSubClient pubSubClient, String channel,
                                            String group, KubeMQHeaderMapper headerMapper) {
        this.pubSubClient = pubSubClient;
        this.channel = channel;
        this.group = group != null ? group : "";
        this.headerMapper = headerMapper;
    }

    @Override
    protected void doStart() {
        log.info("Starting events subscription on channel '{}' (group='{}')", channel, group);
        subscription = EventsSubscription.builder()
                .channel(channel)
                .group(group)
                .onReceiveEventCallback(this::onMessage)
                .onErrorCallback(error ->
                        log.error("Events subscription error on channel '{}': {}", channel, error.getMessage(), error))
                .build();
        pubSubClient.subscribeToEvents(subscription);
    }

    @Override
    protected void doStop() {
        log.info("Stopping events subscription on channel '{}'", channel);
        EventsSubscription sub = this.subscription;
        if (sub != null) {
            sub.cancel();
            this.subscription = null;
        }
    }

    private void onMessage(EventMessageReceived received) {
        Map<String, String> tags = received.getTags();
        MessageHeaders headers = headerMapper.toSpringHeaders(tags);
        Message<byte[]> message = MessageBuilder
                .withPayload(received.getBody() != null ? received.getBody() : new byte[0])
                .copyHeaders(headers)
                .setHeader("kubemq_channel", received.getChannel())
                .setHeader("kubemq_id", received.getId())
                .setHeader("kubemq_metadata", received.getMetadata())
                .build();
        sendMessage(message);
    }
}
