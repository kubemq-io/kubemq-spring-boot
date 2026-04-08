package io.kubemq.spring.cloud.stream.binder.handler;

import io.kubemq.sdk.pubsub.EventSendResult;
import io.kubemq.sdk.pubsub.EventStoreMessage;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter;
import io.kubemq.spring.cloud.stream.binder.KubeMQBinderUtils;
import io.kubemq.spring.cloud.stream.binder.KubeMQHeaderMapper;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

/**
 * Spring Cloud Stream producer handler for KubeMQ Events Store (persistent pub/sub).
 *
 * <p>Converts a Spring {@link Message} into a KubeMQ {@link EventStoreMessage} and publishes
 * it via {@link PubSubClient#publishEventStore(EventStoreMessage)}.
 */
public class KubeMQEventsStoreMessageHandler extends AbstractMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(KubeMQEventsStoreMessageHandler.class);

    private final PubSubClient pubSubClient;
    private final String channel;
    private final KubeMQHeaderMapper headerMapper;
    private final KubeMQMessageConverter messageConverter;

    public KubeMQEventsStoreMessageHandler(PubSubClient pubSubClient, String channel,
                                           KubeMQHeaderMapper headerMapper,
                                           KubeMQMessageConverter messageConverter) {
        this.pubSubClient = pubSubClient;
        this.channel = channel;
        this.headerMapper = headerMapper;
        this.messageConverter = messageConverter;
    }

    @Override
    protected void handleMessageInternal(Message<?> message) {
        byte[] payload = KubeMQBinderUtils.extractPayload(message, messageConverter);
        Map<String, String> tags = headerMapper.toKubeMQTags(message.getHeaders());

        EventStoreMessage storeMessage = EventStoreMessage.builder()
                .channel(channel)
                .body(payload)
                .tags(tags)
                .build();

        log.debug("Publishing event store message to channel '{}'", channel);
        EventSendResult result = pubSubClient.publishEventStore(storeMessage);
        if (!result.isSent()) {
            throw new MessagingException(message,
                    "Failed to publish event store message to channel '" + channel
                            + "': " + result.getError());
        }
    }
}
