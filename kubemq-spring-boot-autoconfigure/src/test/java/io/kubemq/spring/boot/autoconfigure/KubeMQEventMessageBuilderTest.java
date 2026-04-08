package io.kubemq.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import io.kubemq.sdk.pubsub.EventMessage;
import io.kubemq.sdk.pubsub.EventStoreMessage;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.converter.JacksonKubeMQMessageConverter;
import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link io.kubemq.spring.boot.autoconfigure.template.KubeMQEventMessageBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class KubeMQEventMessageBuilderTest {

    @Mock private PubSubClient pubSubClient;
    @Mock private QueuesClient queuesClient;
    @Mock private CQClient cqClient;

    private KubeMQTemplate template;

    @BeforeEach
    void setUp() {
        KubeMQProperties properties = new KubeMQProperties();
        properties.getTemplate().setObservationEnabled(false);
        template = new KubeMQTemplate(
                pubSubClient, queuesClient, cqClient,
                new JacksonKubeMQMessageConverter(new ObjectMapper()),
                null, null, properties);
    }

    @Test
    void metadata_is_wired_to_event_message() {
        template.newEvent("payload")
                .toChannel("ch1")
                .withMetadata("meta-data")
                .send();

        ArgumentCaptor<EventMessage> captor = ArgumentCaptor.forClass(EventMessage.class);
        verify(pubSubClient).publishEvent(captor.capture());
        EventMessage msg = captor.getValue();
        assertThat(msg.getChannel()).isEqualTo("ch1");
        assertThat(msg.getMetadata()).isEqualTo("meta-data");
    }

    @Test
    void null_tags_does_not_throw() {
        // withTags(null) should be safe
        template.newEvent("data")
                .toChannel("ch1")
                .withTags(null)
                .send();

        verify(pubSubClient).publishEvent(any(EventMessage.class));
    }

    @Test
    void event_store_uses_correct_publish_method() {
        template.newEventStore("data")
                .toChannel("es-ch")
                .withMetadata("es-meta")
                .send();

        ArgumentCaptor<EventStoreMessage> captor = ArgumentCaptor.forClass(EventStoreMessage.class);
        verify(pubSubClient).publishEventStore(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo("es-ch");
        assertThat(captor.getValue().getMetadata()).isEqualTo("es-meta");
    }
}
