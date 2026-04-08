package io.kubemq.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.kubemq.sdk.common.ServerInfo;
import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.actuator.KubeMQEndpoint;
import io.kubemq.spring.boot.autoconfigure.actuator.KubeMQEndpoint.ClientStatus;
import io.kubemq.spring.boot.autoconfigure.actuator.KubeMQEndpoint.KubeMQInfo;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link KubeMQEndpoint} actuator endpoint.
 */
@ExtendWith(MockitoExtension.class)
class KubeMQEndpointTest {

    @Mock private PubSubClient pubSubClient;
    @Mock private QueuesClient queuesClient;
    @Mock private CQClient cqClient;

    private KubeMQEndpoint endpoint;

    @BeforeEach
    void setUp() {
        endpoint = new KubeMQEndpoint(pubSubClient, queuesClient, cqClient);
    }

    @Test
    void connected_when_all_clients_succeed() {
        ServerInfo info = ServerInfo.builder()
                .host("node-1").version("2.5.0")
                .serverStartTime(0).serverUpTimeSeconds(100).build();

        when(pubSubClient.ping()).thenReturn(info);
        when(queuesClient.ping()).thenReturn(info);
        when(cqClient.ping()).thenReturn(info);

        KubeMQInfo result = endpoint.kubemqInfo();
        assertThat(result.status()).isEqualTo("connected");
        assertThat(result.clients()).hasSize(3);
        assertThat(result.clients().get("pubsub").status()).isEqualTo("connected");
        assertThat(result.clients().get("queues").status()).isEqualTo("connected");
        assertThat(result.clients().get("cq").status()).isEqualTo("connected");
    }

    @Test
    void degraded_when_one_client_fails() {
        ServerInfo info = ServerInfo.builder()
                .host("node-1").version("2.5.0")
                .serverStartTime(0).serverUpTimeSeconds(100).build();

        when(pubSubClient.ping()).thenReturn(info);
        when(queuesClient.ping()).thenThrow(new RuntimeException("timeout"));
        when(cqClient.ping()).thenReturn(info);

        KubeMQInfo result = endpoint.kubemqInfo();
        assertThat(result.status()).isEqualTo("degraded");
        assertThat(result.clients().get("queues").status()).isEqualTo("disconnected");
        assertThat(result.clients().get("queues").errorType()).isEqualTo("RuntimeException");
    }

    @Test
    void all_three_clients_probed() {
        ServerInfo info = ServerInfo.builder()
                .host("h").version("v")
                .serverStartTime(0).serverUpTimeSeconds(0).build();

        when(pubSubClient.ping()).thenReturn(info);
        when(queuesClient.ping()).thenReturn(info);
        when(cqClient.ping()).thenReturn(info);

        KubeMQInfo result = endpoint.kubemqInfo();
        Map<String, ClientStatus> clients = result.clients();
        assertThat(clients).containsKeys("pubsub", "queues", "cq");
    }

    @Test
    void no_exception_leaks_from_endpoint() {
        when(pubSubClient.ping()).thenThrow(new RuntimeException("boom"));
        when(queuesClient.ping()).thenThrow(new RuntimeException("boom"));
        when(cqClient.ping()).thenThrow(new RuntimeException("boom"));

        // Should not throw -- errors are captured per client
        KubeMQInfo result = endpoint.kubemqInfo();
        assertThat(result.status()).isEqualTo("degraded");
        assertThat(result.clients().values())
                .allMatch(s -> "disconnected".equals(s.status()));
    }
}
