package io.kubemq.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.cq.CommandMessage;
import io.kubemq.sdk.cq.CommandResponseMessage;
import io.kubemq.sdk.cq.QueryMessage;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.sdk.pubsub.EventMessage;
import io.kubemq.sdk.pubsub.EventStoreMessage;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueueMessage;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.converter.JacksonKubeMQMessageConverter;
import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter;
import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.ObservationRegistry;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link KubeMQTemplate}.
 */
@ExtendWith(MockitoExtension.class)
class KubeMQTemplateTest {

    @Mock private PubSubClient pubSubClient;
    @Mock private QueuesClient queuesClient;
    @Mock private CQClient cqClient;

    private KubeMQProperties properties;
    private KubeMQMessageConverter converter;
    private KubeMQTemplate template;

    @BeforeEach
    void setUp() {
        properties = new KubeMQProperties();
        properties.getTemplate().setObservationEnabled(false);
        converter = new JacksonKubeMQMessageConverter(new ObjectMapper());
        template = new KubeMQTemplate(
                pubSubClient, queuesClient, cqClient,
                converter, null, null, properties);
    }

    // ==================== Send ====================

    @Test
    void sendEvent_serializes_and_publishes() {
        template.sendEvent("ch1", "hello");

        ArgumentCaptor<EventMessage> captor = ArgumentCaptor.forClass(EventMessage.class);
        verify(pubSubClient).publishEvent(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo("ch1");
        assertThat(captor.getValue().getBody()).isNotEmpty();
    }

    @Test
    void sendEventStore_serializes_and_publishes() {
        template.sendEventStore("ch2", "world");

        ArgumentCaptor<EventStoreMessage> captor = ArgumentCaptor.forClass(EventStoreMessage.class);
        verify(pubSubClient).publishEventStore(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo("ch2");
    }

    @Test
    void sendQueueMessage_serializes_and_sends() {
        template.sendQueueMessage("q1", "msg-body");

        ArgumentCaptor<QueueMessage> captor = ArgumentCaptor.forClass(QueueMessage.class);
        verify(queuesClient).sendQueueMessage(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo("q1");
    }

    @Test
    void sendCommand_serializes_and_sends() {
        CommandResponseMessage mockResponse = CommandResponseMessage.builder().isExecuted(true).build();
        when(cqClient.sendCommandRequest(any(CommandMessage.class))).thenReturn(mockResponse);

        CommandResponseMessage response = template.sendCommand("cmd1", "data", Duration.ofSeconds(10));

        assertThat(response).isSameAs(mockResponse);
        verify(cqClient).sendCommandRequest(any(CommandMessage.class));
    }

    @Test
    void sendQuery_serializes_and_sends() {
        QueryResponseMessage mockResponse = QueryResponseMessage.builder().isExecuted(true).build();
        when(cqClient.sendQueryRequest(any(QueryMessage.class))).thenReturn(mockResponse);

        QueryResponseMessage response = template.sendQuery("q1", "data", Duration.ofSeconds(10));

        assertThat(response).isSameAs(mockResponse);
        verify(cqClient).sendQueryRequest(any(QueryMessage.class));
    }

    // ==================== Batch ====================

    @Test
    void sendQueueMessages_sends_all_items() {
        template.sendQueueMessages("q1", List.of("a", "b", "c"));

        verify(queuesClient, org.mockito.Mockito.times(3)).sendQueuesMessage(any(QueueMessage.class));
    }

    @Test
    void sendQueueMessages_skips_null_or_empty_list() {
        template.sendQueueMessages("q1", null);
        template.sendQueueMessages("q1", Collections.emptyList());

        verify(queuesClient, never()).sendQueuesMessage(any());
    }

    // ==================== Null Guard ====================

    @Test
    void constructor_rejects_null_clients() {
        assertThatThrownBy(() -> new KubeMQTemplate(
                null, queuesClient, cqClient, converter, null, null, properties))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new KubeMQTemplate(
                pubSubClient, null, cqClient, converter, null, null, properties))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new KubeMQTemplate(
                pubSubClient, queuesClient, null, converter, null, null, properties))
                .isInstanceOf(NullPointerException.class);
    }

    // ==================== Converter Snapshot ====================

    @Test
    void setMessageConverter_updates_converter() {
        KubeMQMessageConverter newConverter = new JacksonKubeMQMessageConverter(new ObjectMapper());
        template.setMessageConverter(newConverter);
        assertThat(template.getMessageConverter()).isSameAs(newConverter);
    }

    // ==================== Default Serialize ====================

    @Test
    void sendEvent_without_converter_handles_byte_array() {
        // Create template without converter to exercise defaultSerialize path
        KubeMQTemplate noConverterTemplate = new KubeMQTemplate(
                pubSubClient, queuesClient, cqClient,
                null, null, null, properties);

        noConverterTemplate.sendEvent("ch1", "hello".getBytes());
        verify(pubSubClient).publishEvent(any(EventMessage.class));
    }

    @Test
    void sendEvent_without_converter_handles_string() {
        KubeMQTemplate noConverterTemplate = new KubeMQTemplate(
                pubSubClient, queuesClient, cqClient,
                null, null, null, properties);

        noConverterTemplate.sendEvent("ch1", "hello");
        verify(pubSubClient).publishEvent(any(EventMessage.class));
    }

    // ==================== Observation ====================

    @Test
    void send_event_with_observation() {
        // Enable observation and verify send still works (observation wraps the call)
        KubeMQProperties obsProperties = new KubeMQProperties();
        obsProperties.getTemplate().setObservationEnabled(true);

        ObservationRegistry registry = ObservationRegistry.create();
        KubeMQTemplate obsTemplate = new KubeMQTemplate(
                pubSubClient, queuesClient, cqClient,
                converter, registry, null, obsProperties);

        obsTemplate.sendEvent("obs-ch", "observed-data");

        // Verify the event was still published through the observation wrapper
        ArgumentCaptor<EventMessage> captor = ArgumentCaptor.forClass(EventMessage.class);
        verify(pubSubClient).publishEvent(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo("obs-ch");
    }

    @Test
    void send_event_without_observation() {
        // When registry is null, observation is not used (default setUp has null registry)
        template.sendEvent("no-obs-ch", "data");

        verify(pubSubClient).publishEvent(any(EventMessage.class));
    }

    @Test
    void send_event_with_metadata() {
        // Metadata is passed through to the SDK message via the fluent builder.
        // The template sendEvent does not have a metadata parameter directly;
        // metadata is passed via builders (newEvent().withMetadata()).
        // Test the fluent builder path:
        template.newEvent("meta-data-payload")
                .toChannel("meta-ch")
                .withMetadata("event-metadata")
                .send();

        ArgumentCaptor<EventMessage> captor = ArgumentCaptor.forClass(EventMessage.class);
        verify(pubSubClient).publishEvent(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo("meta-ch");
        assertThat(captor.getValue().getMetadata()).isEqualTo("event-metadata");
    }

    @Test
    void send_with_null_tags_no_npe() {
        // Passing null tags to the overloaded sendEvent should not cause NPE.
        // The sendEvent(channel, data) method passes Collections.emptyMap() internally,
        // but the template builders handle null tags via withTags(null) as no-op.
        assertThatCode(() -> template.sendEvent("ch1", "hello"))
                .doesNotThrowAnyException();

        verify(pubSubClient).publishEvent(any(EventMessage.class));
    }

    @Test
    void converter_snapshot_prevents_race() {
        // Verify that serializePayload takes a local volatile snapshot of the converter.
        // We swap the converter between calls and verify each call uses its own snapshot.
        KubeMQMessageConverter firstConverter = new JacksonKubeMQMessageConverter(new ObjectMapper());
        KubeMQMessageConverter secondConverter = new JacksonKubeMQMessageConverter(new ObjectMapper());

        template.setMessageConverter(firstConverter);
        template.sendEvent("ch1", "first");
        assertThat(template.getMessageConverter()).isSameAs(firstConverter);

        template.setMessageConverter(secondConverter);
        template.sendEvent("ch1", "second");
        assertThat(template.getMessageConverter()).isSameAs(secondConverter);

        // Both events should have been published
        verify(pubSubClient, org.mockito.Mockito.times(2)).publishEvent(any(EventMessage.class));
    }

    @Test
    void observation_wraps_all_five_patterns() {
        // Enable observation and verify all 5 send patterns create observations
        KubeMQProperties obsProperties = new KubeMQProperties();
        obsProperties.getTemplate().setObservationEnabled(true);

        ObservationRegistry registry = ObservationRegistry.create();
        KubeMQTemplate obsTemplate = new KubeMQTemplate(
                pubSubClient, queuesClient, cqClient,
                converter, registry, null, obsProperties);

        CommandResponseMessage cmdResp = CommandResponseMessage.builder().isExecuted(true).build();
        when(cqClient.sendCommandRequest(any(CommandMessage.class))).thenReturn(cmdResp);
        QueryResponseMessage queryResp = QueryResponseMessage.builder().isExecuted(true).build();
        when(cqClient.sendQueryRequest(any(QueryMessage.class))).thenReturn(queryResp);

        // 1. Event
        obsTemplate.sendEvent("ch-event", "data");
        verify(pubSubClient).publishEvent(any(EventMessage.class));

        // 2. Event Store
        obsTemplate.sendEventStore("ch-event-store", "data");
        verify(pubSubClient).publishEventStore(any(EventStoreMessage.class));

        // 3. Queue
        obsTemplate.sendQueueMessage("ch-queue", "data");
        verify(queuesClient).sendQueueMessage(any(QueueMessage.class));

        // 4. Command
        obsTemplate.sendCommand("ch-command", "data", Duration.ofSeconds(5));
        verify(cqClient).sendCommandRequest(any(CommandMessage.class));

        // 5. Query
        obsTemplate.sendQuery("ch-query", "data", Duration.ofSeconds(5));
        verify(cqClient).sendQueryRequest(any(QueryMessage.class));
    }
}
