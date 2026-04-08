package io.kubemq.spring.boot.autoconfigure.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;

/**
 * Strategy interface for converting application payloads to/from the {@code byte[]} format
 * used by KubeMQ messages.
 *
 * <p>This is a KubeMQ-specific interface and does <b>not</b> extend Spring's
 * {@link org.springframework.messaging.converter.MessageConverter}.
 * The {@code tags} map is mutable: implementations may enrich it (e.g. adding
 * {@code spring-contentType}).
 */
public interface KubeMQMessageConverter {

    /**
     * Serialize a payload to bytes.
     *
     * @param payload the application object to serialize
     * @param tags    mutable tag map; implementations may add entries such as content-type
     * @return serialized bytes
     */
    byte[] toBytes(Object payload, Map<String, String> tags);

    /**
     * Deserialize bytes to the requested target type.
     *
     * @param data       raw bytes from a KubeMQ message
     * @param targetType the class to deserialize into
     * @param <T>        target type
     * @return deserialized object
     */
    <T> T fromBytes(byte[] data, Class<T> targetType);

    /**
     * Deserialize bytes using a Jackson {@link TypeReference} for generic types.
     *
     * @param data    raw bytes from a KubeMQ message
     * @param typeRef the type reference describing the target generic type
     * @param <T>     target type
     * @return deserialized object
     */
    default <T> T fromBytes(byte[] data, TypeReference<T> typeRef) {
        throw new UnsupportedOperationException(
                "TypeReference deserialization not supported by " + getClass().getName());
    }

    /**
     * MIME content type produced by this converter (e.g. {@code "application/json"}).
     */
    String getContentType();
}
