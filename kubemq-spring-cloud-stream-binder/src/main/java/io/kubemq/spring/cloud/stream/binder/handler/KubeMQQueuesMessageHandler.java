package io.kubemq.spring.cloud.stream.binder.handler;

import io.kubemq.sdk.queues.QueueMessage;
import io.kubemq.sdk.queues.QueueSendResult;
import io.kubemq.sdk.queues.QueuesClient;
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
 * Spring Cloud Stream producer handler for KubeMQ Queues.
 *
 * <p>Converts a Spring {@link Message} into a KubeMQ {@link QueueMessage} and sends it
 * via {@link QueuesClient#sendQueueMessage(QueueMessage)} (QueuesUpstream stream API).
 */
public class KubeMQQueuesMessageHandler extends AbstractMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(KubeMQQueuesMessageHandler.class);

    private final QueuesClient queuesClient;
    private final String channel;
    private final KubeMQHeaderMapper headerMapper;
    private final KubeMQMessageConverter messageConverter;

    public KubeMQQueuesMessageHandler(QueuesClient queuesClient, String channel,
                                      KubeMQHeaderMapper headerMapper,
                                      KubeMQMessageConverter messageConverter) {
        this.queuesClient = queuesClient;
        this.channel = channel;
        this.headerMapper = headerMapper;
        this.messageConverter = messageConverter;
    }

    @Override
    protected void handleMessageInternal(Message<?> message) {
        byte[] payload = KubeMQBinderUtils.extractPayload(message, messageConverter);
        Map<String, String> tags = headerMapper.toKubeMQTags(message.getHeaders());

        QueueMessage queueMessage = QueueMessage.builder()
                .channel(channel)
                .body(payload)
                .tags(tags)
                .build();

        log.debug("Sending queue message to channel '{}'", channel);
        QueueSendResult result = queuesClient.sendQueueMessage(queueMessage);
        if (result.isError()) {
            throw new MessagingException(message,
                    "Failed to send queue message to channel '" + channel
                            + "': " + result.getError());
        }
    }
}
