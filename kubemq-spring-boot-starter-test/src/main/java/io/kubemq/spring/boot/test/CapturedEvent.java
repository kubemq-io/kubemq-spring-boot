package io.kubemq.spring.boot.test;

import kubemq.Kubemq;

import java.util.Map;

/**
 * Immutable domain wrapper around a captured protobuf {@link Kubemq.Event}.
 *
 * <p>Provides a test-friendly view of events received by {@link MockKubeMQService},
 * decoupling test assertions from protobuf internals.
 */
public record CapturedEvent(
        String channel, String id, byte[] body, String metadata, Map<String, String> tags) {

    /**
     * Creates a {@code CapturedEvent} from a protobuf {@link Kubemq.Event}.
     */
    public static CapturedEvent from(Kubemq.Event event) {
        return new CapturedEvent(event.getChannel(), event.getEventID(),
                event.getBody().toByteArray(), event.getMetadata(), event.getTagsMap());
    }
}
