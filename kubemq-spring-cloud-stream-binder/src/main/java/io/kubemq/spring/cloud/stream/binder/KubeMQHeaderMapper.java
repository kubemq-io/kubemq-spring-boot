package io.kubemq.spring.cloud.stream.binder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.messaging.MessageHeaders;

/**
 * Bidirectional mapper between Spring {@link MessageHeaders} and KubeMQ tags.
 *
 * <p>Outbound: Spring headers become KubeMQ tags (string key-value pairs).
 * Special Spring headers ({@code id}, {@code timestamp}, {@code contentType}) are prefixed
 * with {@code "spring-"} to avoid collision. Trace propagation headers are always preserved.
 *
 * <p>Inbound: KubeMQ tags become Spring headers. Tags prefixed with {@code "spring-"} are
 * restored to their original header names.
 */
public class KubeMQHeaderMapper {

    private static final String SPRING_PREFIX = "spring-";
    private static final String KUBEMQ_TAG_PREFIX = "kubemq.tag.";
    private static final Set<String> ALLOWED_PREFIXES = Set.of("kubemq-", "spring-", "app-");

    private static final Set<String> SPECIAL_SPRING_HEADERS = Set.of(
            "id", "timestamp", "contentType"
    );

    private static final Set<String> TRACE_HEADERS = Set.of(
            "kubemq-traceparent", "kubemq-tracestate"
    );

    /**
     * Maps Spring message headers to KubeMQ tags for outbound messages.
     *
     * @param springHeaders the Spring message headers
     * @return KubeMQ tags map (string key-value)
     */
    public Map<String, String> toKubeMQTags(MessageHeaders springHeaders) {
        if (springHeaders == null || springHeaders.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, String> tags = new HashMap<>();
        for (Map.Entry<String, Object> entry : springHeaders.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (TRACE_HEADERS.contains(key)) {
                tags.put(key, value.toString());
            } else if (SPECIAL_SPRING_HEADERS.contains(key)) {
                tags.put(SPRING_PREFIX + key, value.toString());
            } else {
                tags.put(key, value.toString());
            }
        }
        return tags;
    }

    /**
     * Maps KubeMQ tags to Spring message headers for inbound messages.
     *
     * @param kubemqTags the KubeMQ tags
     * @return Spring message headers
     */
    public MessageHeaders toSpringHeaders(Map<String, String> kubemqTags) {
        if (kubemqTags == null || kubemqTags.isEmpty()) {
            return new MessageHeaders(null);
        }
        Map<String, Object> headers = new HashMap<>();
        for (Map.Entry<String, String> entry : kubemqTags.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(SPRING_PREFIX)) {
                headers.put(key.substring(SPRING_PREFIX.length()), entry.getValue());
            } else if (hasAllowedPrefix(key)) {
                headers.put(key, entry.getValue());
            } else {
                headers.put(KUBEMQ_TAG_PREFIX + key, entry.getValue());
            }
        }
        return new MessageHeaders(headers);
    }

    private boolean hasAllowedPrefix(String key) {
        for (String prefix : ALLOWED_PREFIXES) {
            if (key.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
