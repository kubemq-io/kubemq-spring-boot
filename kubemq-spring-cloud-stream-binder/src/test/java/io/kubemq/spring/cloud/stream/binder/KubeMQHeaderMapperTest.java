package io.kubemq.spring.cloud.stream.binder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KubeMQHeaderMapperTest {

    private KubeMQHeaderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new KubeMQHeaderMapper();
    }

    @Test
    void toKubeMQTags_allowedPrefixHeaders_passthroughUnchanged() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("kubemq-traceparent", "00-abc-def-01");
        headers.put("kubemq-tracestate", "vendor=value");
        headers.put("app-custom", "value1");
        MessageHeaders springHeaders = new MessageHeaders(headers);

        Map<String, String> tags = mapper.toKubeMQTags(springHeaders);

        assertThat(tags).containsEntry("kubemq-traceparent", "00-abc-def-01");
        assertThat(tags).containsEntry("kubemq-tracestate", "vendor=value");
        assertThat(tags).containsEntry("app-custom", "value1");
    }

    @Test
    void toKubeMQTags_specialSpringHeaders_getPrefixed() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("contentType", "application/json");
        MessageHeaders springHeaders = new MessageHeaders(headers);

        Map<String, String> tags = mapper.toKubeMQTags(springHeaders);

        assertThat(tags).containsEntry("spring-contentType", "application/json");
        assertThat(tags).doesNotContainKey("contentType");
    }

    @Test
    void toKubeMQTags_nullHeaders_returnsEmptyMap() {
        Map<String, String> tags = mapper.toKubeMQTags(null);
        assertThat(tags).isEmpty();
    }

    @Test
    void toSpringHeaders_springPrefixedTags_restoreOriginalName() {
        Map<String, String> tags = Map.of(
                "spring-contentType", "application/json"
        );

        MessageHeaders headers = mapper.toSpringHeaders(tags);

        // "spring-contentType" tag should be restored to "contentType" header
        assertThat(headers.get("contentType")).isEqualTo("application/json");
        // The original prefixed key should not exist
        assertThat(headers.containsKey("spring-contentType")).isFalse();
    }

    @Test
    void toSpringHeaders_unknownPrefixTags_getKubemqTagPrefix() {
        Map<String, String> tags = Map.of(
                "custom-header", "custom-value",
                "another", "val"
        );

        MessageHeaders headers = mapper.toSpringHeaders(tags);

        // Unknown prefix tags get "kubemq.tag." prefix
        assertThat(headers.get("kubemq.tag.custom-header")).isEqualTo("custom-value");
        assertThat(headers.get("kubemq.tag.another")).isEqualTo("val");
    }

    @Test
    void toSpringHeaders_allowedPrefixTags_passthroughUnchanged() {
        Map<String, String> tags = Map.of(
                "kubemq-traceparent", "trace-value",
                "app-key", "app-value"
        );

        MessageHeaders headers = mapper.toSpringHeaders(tags);

        assertThat(headers.get("kubemq-traceparent")).isEqualTo("trace-value");
        assertThat(headers.get("app-key")).isEqualTo("app-value");
    }

    @Test
    void toSpringHeaders_nullTags_returnsEmptyHeaders() {
        MessageHeaders headers = mapper.toSpringHeaders(null);
        // MessageHeaders with null map still works (has id + timestamp)
        assertThat(headers).isNotNull();
    }
}
