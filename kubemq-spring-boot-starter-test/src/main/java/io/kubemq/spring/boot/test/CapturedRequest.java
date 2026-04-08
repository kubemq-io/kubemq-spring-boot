package io.kubemq.spring.boot.test;

import kubemq.Kubemq;

import java.util.Map;

/**
 * Immutable domain wrapper around a captured protobuf {@link Kubemq.Request}.
 *
 * <p>Provides a test-friendly view of command/query requests received by
 * {@link MockKubeMQService}, decoupling test assertions from protobuf internals.
 */
public record CapturedRequest(
        String channel, String requestId, byte[] body, String metadata,
        Map<String, String> tags, String requestType) {

    /**
     * Creates a {@code CapturedRequest} from a protobuf {@link Kubemq.Request}.
     */
    public static CapturedRequest from(Kubemq.Request request) {
        return new CapturedRequest(request.getChannel(), request.getRequestID(),
                request.getBody().toByteArray(), request.getMetadata(),
                request.getTagsMap(), request.getRequestTypeData().name());
    }
}
