package io.kubemq.spring.cloud.stream.binder;

import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/**
 * Utility methods shared across the KubeMQ binder components.
 */
public final class KubeMQBinderUtils {

    private KubeMQBinderUtils() {
    }

    /**
     * Extracts the payload from a Spring message as a byte array.
     * Handles {@code byte[]}, {@code String}, delegates to the converter for other types,
     * and throws if no converter is available for non-trivial payloads.
     */
    public static byte[] extractPayload(Message<?> message, KubeMQMessageConverter converter) {
        Object payload = message.getPayload();
        if (payload == null) {
            return new byte[0];
        }
        if (payload instanceof byte[] bytes) {
            return bytes;
        }
        if (payload instanceof String str) {
            return str.getBytes(StandardCharsets.UTF_8);
        }
        if (converter != null) {
            return converter.toBytes(payload, new HashMap<>());
        }
        throw new IllegalArgumentException(
                "Cannot serialize payload of type " + payload.getClass().getName()
                        + ". Configure a KubeMQMessageConverter bean.");
    }

    /**
     * Extracts the payload from a Spring message as a byte array.
     * Handles {@code byte[]}, {@code String}, and falls back to {@code toString().getBytes()}.
     *
     * @deprecated Use {@link #extractPayload(Message, KubeMQMessageConverter)} instead.
     */
    @Deprecated
    public static byte[] extractPayload(Message<?> message) {
        Object payload = message.getPayload();
        if (payload instanceof byte[] bytes) {
            return bytes;
        }
        if (payload instanceof String str) {
            return str.getBytes(StandardCharsets.UTF_8);
        }
        return payload.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Returns the content type from message headers, or {@code "application/octet-stream"}
     * if none is set.
     */
    public static String resolveContentType(MessageHeaders headers) {
        Object contentType = headers.get(MessageHeaders.CONTENT_TYPE);
        if (contentType != null) {
            return contentType.toString();
        }
        return "application/octet-stream";
    }

    /**
     * Returns the group string, defaulting to empty if null.
     */
    public static String normalizeGroup(String group) {
        return group != null ? group : "";
    }
}
