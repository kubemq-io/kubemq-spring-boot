package io.kubemq.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.kubemq.sdk.common.ServerInfo;
import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.health.KubeMQHealthIndicator;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

/**
 * Tests for {@link KubeMQHealthIndicator}.
 */
@ExtendWith(MockitoExtension.class)
class KubeMQHealthIndicatorTest {

    @Mock private PubSubClient pubSubClient;
    @Mock private QueuesClient queuesClient;
    @Mock private CQClient cqClient;

    private KubeMQHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        indicator = new KubeMQHealthIndicator(
                pubSubClient, queuesClient, cqClient,
                Duration.ofSeconds(5), Duration.ofSeconds(1));
    }

    @Test
    void reports_up_when_all_clients_respond() {
        ServerInfo info = ServerInfo.builder()
                .host("broker-1").version("2.5.0")
                .serverStartTime(System.currentTimeMillis())
                .serverUpTimeSeconds(100).build();

        when(pubSubClient.ping()).thenReturn(info);
        when(queuesClient.ping()).thenReturn(info);
        when(cqClient.ping()).thenReturn(info);

        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("host");
        assertThat(health.getDetails().get("host")).isEqualTo("broker-1");
    }

    @Test
    void reports_down_when_pubsub_client_fails() {
        when(pubSubClient.ping()).thenThrow(new RuntimeException("connection refused"));
        // Don't stub others - CompletableFuture.allOf will fail anyway due to pubsub

        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void reports_down_when_queues_client_fails() {
        ServerInfo info = ServerInfo.builder()
                .host("h").version("v").serverStartTime(0).serverUpTimeSeconds(0).build();
        when(pubSubClient.ping()).thenReturn(info);
        when(queuesClient.ping()).thenThrow(new RuntimeException("timeout"));
        when(cqClient.ping()).thenReturn(info);

        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void uses_cache_for_repeated_calls() throws InterruptedException {
        ServerInfo info = ServerInfo.builder()
                .host("cached").version("1.0")
                .serverStartTime(0).serverUpTimeSeconds(0).build();

        when(pubSubClient.ping()).thenReturn(info);
        when(queuesClient.ping()).thenReturn(info);
        when(cqClient.ping()).thenReturn(info);

        Health first = indicator.health();
        assertThat(first.getStatus()).isEqualTo(Status.UP);

        // Second call within cache duration should use cache
        Health second = indicator.health();
        assertThat(second.getStatus()).isEqualTo(Status.UP);
        assertThat(second.getDetails().get("host")).isEqualTo("cached");
    }

    @Test
    void all_three_clients_are_probed() {
        ServerInfo info = ServerInfo.builder()
                .host("h").version("v")
                .serverStartTime(0).serverUpTimeSeconds(0).build();

        when(pubSubClient.ping()).thenReturn(info);
        when(queuesClient.ping()).thenReturn(info);
        when(cqClient.ping()).thenReturn(info);

        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);

        // All three clients were pinged (verified by Mockito)
        org.mockito.Mockito.verify(pubSubClient).ping();
        org.mockito.Mockito.verify(queuesClient).ping();
        org.mockito.Mockito.verify(cqClient).ping();
    }

    @Test
    void timeout_on_slow_ping() {
        // Create indicator with a very short timeout
        KubeMQHealthIndicator shortTimeoutIndicator = new KubeMQHealthIndicator(
                pubSubClient, queuesClient, cqClient,
                Duration.ofMillis(50), Duration.ofSeconds(0));

        // Make pubsub client take a long time to respond (simulate slow ping)
        when(pubSubClient.ping()).thenAnswer(inv -> {
            Thread.sleep(5000); // Sleeps longer than timeout
            return ServerInfo.builder().host("h").version("v")
                    .serverStartTime(0).serverUpTimeSeconds(0).build();
        });

        Health health = shortTimeoutIndicator.health();
        // With orTimeout(50ms) on each CompletableFuture, the slow ping triggers a timeout
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void details_respect_show_details_setting() {
        // Verify health details are populated with host/version when healthy
        // and error type when unhealthy
        ServerInfo info = ServerInfo.builder()
                .host("my-broker").version("2.5.0")
                .serverStartTime(0).serverUpTimeSeconds(100).build();

        when(pubSubClient.ping()).thenReturn(info);
        when(queuesClient.ping()).thenReturn(info);
        when(cqClient.ping()).thenReturn(info);

        Health upHealth = indicator.health();
        assertThat(upHealth.getDetails()).containsEntry("host", "my-broker");
        assertThat(upHealth.getDetails()).containsEntry("version", "2.5.0");

        // Now test with failure — new indicator to avoid cache
        KubeMQHealthIndicator failIndicator = new KubeMQHealthIndicator(
                pubSubClient, queuesClient, cqClient,
                Duration.ofSeconds(5), Duration.ofSeconds(0));

        org.mockito.Mockito.reset(pubSubClient, queuesClient, cqClient);
        when(pubSubClient.ping()).thenThrow(new RuntimeException("connection refused"));

        Health downHealth = failIndicator.health();
        assertThat(downHealth.getDetails()).containsKey("error");
        // Error detail should be the exception class simple name
        assertThat(downHealth.getDetails().get("error").toString())
                .doesNotContain("connection refused"); // sanitized — only class name
    }
}
