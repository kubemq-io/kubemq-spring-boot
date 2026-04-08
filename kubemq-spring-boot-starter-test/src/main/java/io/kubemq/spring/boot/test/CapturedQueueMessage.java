package io.kubemq.spring.boot.test;

import kubemq.Kubemq;

import java.util.Map;

/**
 * Immutable domain wrapper around a captured protobuf {@link Kubemq.QueueMessage}.
 *
 * <p>Provides a test-friendly view of queue messages received by
 * {@link MockKubeMQService}, decoupling test assertions from protobuf internals.
 */
public record CapturedQueueMessage(
        String channel, String messageId, byte[] body, String metadata, Map<String, String> tags) {

    /**
     * Creates a {@code CapturedQueueMessage} from a protobuf {@link Kubemq.QueueMessage}.
     */
    public static CapturedQueueMessage from(Kubemq.QueueMessage message) {
        return new CapturedQueueMessage(message.getChannel(), message.getMessageID(),
                message.getBody().toByteArray(), message.getMetadata(), message.getTagsMap());
    }
}
