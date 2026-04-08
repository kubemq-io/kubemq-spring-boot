package io.kubemq.spring.cloud.stream.binder.handler;

import io.kubemq.sdk.pubsub.EventMessage;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter;
import io.kubemq.spring.cloud.stream.binder.KubeMQBinderUtils;
import io.kubemq.spring.cloud.stream.binder.KubeMQHeaderMapper;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;

/**
 * Spring Cloud Stream producer handler for KubeMQ Events (fire-and-forget pub/sub).
 *
 * <p>Converts a Spring {@link Message} into a KubeMQ {@link EventMessage} and publishes
 * it via {@link PubSubClient#publishEvent(EventMessage)}.
 */
public class KubeMQEventsMessageHandler extends AbstractMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(KubeMQEventsMessageHandler.class);

    private final PubSubClient pubSubClient;
    private final String channel;
    private final KubeMQHeaderMapper headerMapper;
    private final KubeMQMessageConverter messageConverter;

    public KubeMQEventsMessageHandler(PubSubClient pubSubClient, String channel,
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

        EventMessage eventMessage = EventMessage.builder()
                .channel(channel)
                .body(payload)
                .tags(tags)
                .build();

        log.debug("Publishing event to channel '{}'", channel);
        pubSubClient.publishEvent(eventMessage);
    }
}
