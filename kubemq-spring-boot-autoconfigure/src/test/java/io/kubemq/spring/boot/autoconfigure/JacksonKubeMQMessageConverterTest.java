package io.kubemq.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubemq.spring.boot.autoconfigure.converter.JacksonKubeMQMessageConverter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link JacksonKubeMQMessageConverter}.
 */
class JacksonKubeMQMessageConverterTest {

    private JacksonKubeMQMessageConverter converter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        converter = new JacksonKubeMQMessageConverter(objectMapper);
    }

    // ==================== Null Guard ====================

    @Test
    void toBytes_null_returns_empty_array() {
        byte[] result = converter.toBytes(null, new HashMap<>());
        assertThat(result).isEmpty();
    }

    @Test
    void fromBytes_null_returns_null() {
        String result = converter.fromBytes(null, String.class);
        assertThat(result).isNull();
    }

    @Test
    void fromBytes_empty_returns_null() {
        String result = converter.fromBytes(new byte[0], String.class);
        assertThat(result).isNull();
    }

    // ==================== Byte Passthrough ====================

    @Test
    void toBytes_byte_array_passthrough() {
        byte[] input = {1, 2, 3};
        Map<String, String> tags = new HashMap<>();
        byte[] result = converter.toBytes(input, tags);
        assertThat(result).isSameAs(input);
        assertThat(tags).containsKey("spring-contentType");
    }

    @Test
    void fromBytes_byte_array_passthrough() {
        byte[] input = {4, 5, 6};
        byte[] result = converter.fromBytes(input, byte[].class);
        assertThat(result).isSameAs(input);
    }

    // ==================== String ====================

    @Test
    void toBytes_string_uses_utf8() {
        Map<String, String> tags = new HashMap<>();
        byte[] result = converter.toBytes("hello", tags);
        assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo("hello");
    }

    @Test
    void fromBytes_string_uses_utf8() {
        byte[] input = "hello".getBytes(StandardCharsets.UTF_8);
        String result = converter.fromBytes(input, String.class);
        assertThat(result).isEqualTo("hello");
    }

    // ==================== Round Trip (JSON) ====================

    @Test
    void round_trip_pojo() {
        TestPayload original = new TestPayload("foo", 42);
        Map<String, String> tags = new HashMap<>();
        byte[] bytes = converter.toBytes(original, tags);
        assertThat(bytes).isNotEmpty();

        TestPayload restored = converter.fromBytes(bytes, TestPayload.class);
        assertThat(restored.name).isEqualTo("foo");
        assertThat(restored.value).isEqualTo(42);
    }

    // ==================== TypeReference ====================

    @Test
    void fromBytes_typeReference() throws Exception {
        List<String> original = List.of("a", "b", "c");
        byte[] bytes = objectMapper.writeValueAsBytes(original);

        List<String> result = converter.fromBytes(bytes, new TypeReference<List<String>>() {});
        assertThat(result).containsExactly("a", "b", "c");
    }

    @Test
    void fromBytes_typeReference_null_returns_null() {
        List<String> result = converter.fromBytes(null, new TypeReference<List<String>>() {});
        assertThat(result).isNull();
    }

    // ==================== Content Type ====================

    @Test
    void getContentType_returns_json() {
        assertThat(converter.getContentType()).isEqualTo("application/json");
    }

    // ==================== Test Payload ====================

    public static class TestPayload {
        public String name;
        public int value;

        public TestPayload() {}

        public TestPayload(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}
