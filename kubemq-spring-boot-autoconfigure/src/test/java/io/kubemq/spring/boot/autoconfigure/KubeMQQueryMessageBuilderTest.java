package io.kubemq.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.cq.QueryMessage;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.converter.JacksonKubeMQMessageConverter;
import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link io.kubemq.spring.boot.autoconfigure.template.KubeMQQueryMessageBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class KubeMQQueryMessageBuilderTest {

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
    void metadata_is_wired_to_query_message() {
        QueryResponseMessage mockResp = QueryResponseMessage.builder().isExecuted(true).build();
        when(cqClient.sendQueryRequest(any(QueryMessage.class))).thenReturn(mockResp);

        QueryResponseMessage response = template.newQuery("query-data")
                .toChannel("query-ch")
                .withMetadata("query-meta")
                .withTimeout(Duration.ofSeconds(5))
                .send();

        assertThat(response.isExecuted()).isTrue();
        ArgumentCaptor<QueryMessage> captor = ArgumentCaptor.forClass(QueryMessage.class);
        verify(cqClient).sendQueryRequest(captor.capture());
        assertThat(captor.getValue().getChannel()).isEqualTo("query-ch");
        assertThat(captor.getValue().getMetadata()).isEqualTo("query-meta");
    }

    @Test
    void null_tags_does_not_throw() {
        QueryResponseMessage mockResp = QueryResponseMessage.builder().isExecuted(true).build();
        when(cqClient.sendQueryRequest(any(QueryMessage.class))).thenReturn(mockResp);

        template.newQuery("data")
                .toChannel("q-ch")
                .withTags(null)
                .withTimeout(Duration.ofSeconds(3))
                .send();

        verify(cqClient).sendQueryRequest(any(QueryMessage.class));
    }
}
