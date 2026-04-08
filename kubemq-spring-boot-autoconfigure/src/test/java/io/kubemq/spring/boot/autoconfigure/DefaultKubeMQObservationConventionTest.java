package io.kubemq.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.kubemq.spring.boot.autoconfigure.observation.DefaultKubeMQObservationConvention;
import io.kubemq.spring.boot.autoconfigure.observation.KubeMQReceiveObservationContext;
import io.kubemq.spring.boot.autoconfigure.observation.KubeMQSendObservationContext;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DefaultKubeMQObservationConvention}.
 */
class DefaultKubeMQObservationConventionTest {

    private DefaultKubeMQObservationConvention convention;

    @BeforeEach
    void setUp() {
        convention = new DefaultKubeMQObservationConvention();
    }

    @Test
    void name_is_kubemq() {
        assertThat(convention.getName()).isEqualTo("kubemq");
    }

    @Test
    void contextualName_for_send() {
        KubeMQSendObservationContext ctx = new KubeMQSendObservationContext("my-channel", "EVENTS");
        assertThat(convention.getContextualName(ctx)).isEqualTo("kubemq events send");
    }

    @Test
    void contextualName_for_receive() {
        KubeMQReceiveObservationContext ctx = new KubeMQReceiveObservationContext("my-channel", "QUEUES");
        assertThat(convention.getContextualName(ctx)).isEqualTo("kubemq queues receive");
    }

    @Test
    void lowCardinality_contains_pattern() {
        KubeMQSendObservationContext ctx = new KubeMQSendObservationContext("ch1", "EVENTS_STORE");
        KeyValues keyValues = convention.getLowCardinalityKeyValues(ctx);
        assertThat(keyValues.stream().map(KeyValue::getKey))
                .contains("kubemq.pattern");
        assertThat(keyValues.stream().filter(kv -> "kubemq.pattern".equals(kv.getKey()))
                .map(KeyValue::getValue).findFirst().orElse(""))
                .isEqualTo("EVENTS_STORE");
    }

    @Test
    void highCardinality_contains_channel() {
        KubeMQSendObservationContext ctx = new KubeMQSendObservationContext("orders.topic", "EVENTS");
        KeyValues keyValues = convention.getHighCardinalityKeyValues(ctx);
        assertThat(keyValues.stream().map(KeyValue::getKey))
                .contains("kubemq.channel");
        assertThat(keyValues.stream().filter(kv -> "kubemq.channel".equals(kv.getKey()))
                .map(KeyValue::getValue).findFirst().orElse(""))
                .isEqualTo("orders.topic");
    }

    @Test
    void lowCardinality_for_receive_context() {
        KubeMQReceiveObservationContext ctx = new KubeMQReceiveObservationContext("ch", "COMMANDS");
        KeyValues keyValues = convention.getLowCardinalityKeyValues(ctx);
        assertThat(keyValues.stream().filter(kv -> "kubemq.pattern".equals(kv.getKey()))
                .map(KeyValue::getValue).findFirst().orElse(""))
                .isEqualTo("COMMANDS");
    }

    @Test
    void highCardinality_for_receive_context() {
        KubeMQReceiveObservationContext ctx = new KubeMQReceiveObservationContext("recv-ch", "QUERIES");
        KeyValues keyValues = convention.getHighCardinalityKeyValues(ctx);
        assertThat(keyValues.stream().filter(kv -> "kubemq.channel".equals(kv.getKey()))
                .map(KeyValue::getValue).findFirst().orElse(""))
                .isEqualTo("recv-ch");
    }
}
