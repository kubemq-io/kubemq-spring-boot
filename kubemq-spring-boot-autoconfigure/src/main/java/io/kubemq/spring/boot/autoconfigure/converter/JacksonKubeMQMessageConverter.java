package io.kubemq.spring.boot.autoconfigure.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Default {@link KubeMQMessageConverter} backed by Jackson {@link ObjectMapper}.
 *
 * <p>Pass-through semantics for {@code byte[]} and {@code String} payloads;
 * all other types are serialized/deserialized as JSON.
 */
public class JacksonKubeMQMessageConverter implements KubeMQMessageConverter {

    private static final String CONTENT_TYPE = "application/json";
    private static final String TAG_CONTENT_TYPE = "spring-contentType";

    private final ObjectMapper objectMapper;

    public JacksonKubeMQMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] toBytes(Object payload, Map<String, String> tags) {
        if (payload == null) {
            return new byte[0];
        }
        if (tags != null) {
            tags.putIfAbsent(TAG_CONTENT_TYPE, CONTENT_TYPE);
        }
        if (payload instanceof byte[] bytes) {
            return bytes;
        }
        if (payload instanceof String str) {
            return str.getBytes(StandardCharsets.UTF_8);
        }
        try {
            return objectMapper.writeValueAsBytes(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "Failed to serialize payload of type " + payload.getClass().getName() + " to JSON", e);
        }
    }

    @Override
    public <T> T fromBytes(byte[] data, Class<T> targetType) {
        if (data == null || data.length == 0) {
            return null;
        }
        if (targetType == byte[].class) {
            return targetType.cast(data);
        }
        if (targetType == String.class) {
            return targetType.cast(new String(data, StandardCharsets.UTF_8));
        }
        try {
            return objectMapper.readValue(data, targetType);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Failed to deserialize JSON to " + targetType.getName(), e);
        }
    }

    @Override
    public <T> T fromBytes(byte[] data, TypeReference<T> typeRef) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(data, typeRef);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Failed to deserialize JSON to " + typeRef.getType(), e);
        }
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }
}
