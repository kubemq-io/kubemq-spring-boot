package io.kubemq.spring.cloud.stream.binder;

import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KubeMQBinderUtilsTest {

    @Test
    void extractPayload_nullPayload_returnsEmptyByteArray() {
        // The overloaded method with converter handles null payload
        Message<?> message = MessageBuilder.withPayload(new byte[0]).build();
        byte[] result = KubeMQBinderUtils.extractPayload(message, null);
        assertThat(result).isEmpty();
    }

    @Test
    void extractPayload_byteArrayPayload_returnsSameBytes() {
        byte[] payload = "hello".getBytes(StandardCharsets.UTF_8);
        Message<byte[]> message = MessageBuilder.withPayload(payload).build();
        byte[] result = KubeMQBinderUtils.extractPayload(message, null);
        assertThat(result).isEqualTo(payload);
    }

    @Test
    void extractPayload_stringPayload_returnsUtf8Bytes() {
        String payload = "hello world";
        Message<String> message = MessageBuilder.withPayload(payload).build();
        byte[] result = KubeMQBinderUtils.extractPayload(message, null);
        assertThat(result).isEqualTo(payload.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void extractPayload_pojoPayload_delegatesToConverter() {
        Object pojo = new Object() {
            @Override
            public String toString() {
                return "test-pojo";
            }
        };
        byte[] expectedBytes = "serialized".getBytes(StandardCharsets.UTF_8);

        KubeMQMessageConverter converter = mock(KubeMQMessageConverter.class);
        when(converter.toBytes(eq(pojo), any())).thenReturn(expectedBytes);

        Message<Object> message = MessageBuilder.withPayload(pojo).build();
        byte[] result = KubeMQBinderUtils.extractPayload(message, converter);
        assertThat(result).isEqualTo(expectedBytes);
    }

    @Test
    void extractPayload_pojoPayload_noConverter_throwsIllegalArgumentException() {
        Object pojo = new Object() {
            @Override
            public String toString() {
                return "test-pojo";
            }
        };
        Message<Object> message = MessageBuilder.withPayload(pojo).build();

        assertThatThrownBy(() -> KubeMQBinderUtils.extractPayload(message, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot serialize payload")
                .hasMessageContaining("KubeMQMessageConverter");
    }

    @Test
    void resolveContentType_withContentTypeHeader_returnsValue() {
        Message<byte[]> message = MessageBuilder.withPayload(new byte[0])
                .setHeader("contentType", "application/json")
                .build();
        String contentType = KubeMQBinderUtils.resolveContentType(message.getHeaders());
        assertThat(contentType).isEqualTo("application/json");
    }

    @Test
    void resolveContentType_noContentTypeHeader_returnsOctetStream() {
        Message<byte[]> message = MessageBuilder.withPayload(new byte[0]).build();
        String contentType = KubeMQBinderUtils.resolveContentType(message.getHeaders());
        assertThat(contentType).isEqualTo("application/octet-stream");
    }

    @Test
    void normalizeGroup_nullGroup_returnsEmptyString() {
        assertThat(KubeMQBinderUtils.normalizeGroup(null)).isEmpty();
    }

    @Test
    void normalizeGroup_nonNullGroup_returnsSameValue() {
        assertThat(KubeMQBinderUtils.normalizeGroup("my-group")).isEqualTo("my-group");
    }
}
