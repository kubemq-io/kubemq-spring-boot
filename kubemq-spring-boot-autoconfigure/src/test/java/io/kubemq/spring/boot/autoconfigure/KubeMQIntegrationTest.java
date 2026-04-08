package io.kubemq.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.cq.CommandMessage;
import io.kubemq.sdk.cq.CommandResponseMessage;
import io.kubemq.sdk.cq.QueryMessage;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.sdk.pubsub.EventMessage;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueueMessage;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Integration tests for KubeMQ auto-configuration.
 *
 * <p>These tests validate the auto-configuration wiring works correctly
 * using {@link ApplicationContextRunner}. They do NOT require a live broker.
 */
class KubeMQIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    KubeMQAutoConfiguration.class,
                    KubeMQTemplateAutoConfiguration.class,
                    KubeMQListenerAutoConfiguration.class))
            .withBean(ObjectMapper.class, ObjectMapper::new)
            .withPropertyValues(
                    "kubemq.address=localhost:50000",
                    "kubemq.client-id=integration-test");

    @Test
    void auto_config_creates_all_beans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(PubSubClient.class);
            assertThat(context).hasSingleBean(QueuesClient.class);
            assertThat(context).hasSingleBean(CQClient.class);
        });
    }

    @Test
    void template_auto_config_creates_template() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(KubeMQTemplate.class);
        });
    }

    @Test
    void listener_auto_config_creates_registrar_and_factory() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(
                    io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerContainerFactory.class);
            assertThat(context).hasSingleBean(
                    io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerEndpointRegistrar.class);
            assertThat(context).hasSingleBean(
                    io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerAnnotationBeanPostProcessor.class);
        });
    }

    @Test
    void properties_are_bound() {
        contextRunner.run(context -> {
            KubeMQProperties props = context.getBean(KubeMQProperties.class);
            assertThat(props.getAddress()).isEqualTo("localhost:50000");
            assertThat(props.getClientId()).isEqualTo("integration-test");
        });
    }

    @Test
    void disabled_config_produces_no_beans() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        KubeMQAutoConfiguration.class,
                        KubeMQTemplateAutoConfiguration.class))
                .withBean(ObjectMapper.class, ObjectMapper::new)
                .withPropertyValues("kubemq.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(PubSubClient.class);
                    assertThat(context).doesNotHaveBean(KubeMQTemplate.class);
                });
    }

    @Test
    void custom_properties_override_defaults() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(KubeMQAutoConfiguration.class))
                .withBean(ObjectMapper.class, ObjectMapper::new)
                .withPropertyValues(
                        "kubemq.address=broker:9000",
                        "kubemq.client-id=custom-id",
                        "kubemq.listener.concurrency=4",
                        "kubemq.listener.auto-startup=false",
                        "kubemq.listener.shutdown-timeout=10s",
                        "kubemq.template.observation-enabled=false")
                .run(context -> {
                    KubeMQProperties props = context.getBean(KubeMQProperties.class);
                    assertThat(props.getAddress()).isEqualTo("broker:9000");
                    assertThat(props.getListener().getConcurrency()).isEqualTo(4);
                    assertThat(props.getListener().isAutoStartup()).isFalse();
                    assertThat(props.getTemplate().isObservationEnabled()).isFalse();
                });
    }

    // ==================== End-to-End (mock clients) ====================

    @Test
    void end_to_end_send_event_and_capture() {
        // Use mock clients to verify an event sent via KubeMQTemplate reaches the PubSubClient
        PubSubClient mockPubSub = mock(PubSubClient.class);

        contextRunner
                .withBean("kubemqPubSubClient", PubSubClient.class, () -> mockPubSub)
                .run(context -> {
                    KubeMQTemplate template = context.getBean(KubeMQTemplate.class);
                    template.sendEvent("e2e-event-ch", "event-payload");

                    ArgumentCaptor<EventMessage> captor = ArgumentCaptor.forClass(EventMessage.class);
                    verify(mockPubSub).publishEvent(captor.capture());
                    assertThat(captor.getValue().getChannel()).isEqualTo("e2e-event-ch");
                    assertThat(captor.getValue().getBody()).isNotEmpty();
                });
    }

    @Test
    void end_to_end_command_request_response() {
        // Use mock CQClient to verify command request-response through KubeMQTemplate
        CQClient mockCq = mock(CQClient.class);
        CommandResponseMessage expectedResp = CommandResponseMessage.builder()
                .isExecuted(true).build();
        when(mockCq.sendCommandRequest(any(CommandMessage.class))).thenReturn(expectedResp);

        contextRunner
                .withBean("kubemqCQClient", CQClient.class, () -> mockCq)
                .run(context -> {
                    KubeMQTemplate template = context.getBean(KubeMQTemplate.class);
                    CommandResponseMessage response = template.sendCommand(
                            "e2e-cmd-ch", "cmd-data", Duration.ofSeconds(10));

                    assertThat(response).isSameAs(expectedResp);
                    assertThat(response.isExecuted()).isTrue();
                    verify(mockCq).sendCommandRequest(any(CommandMessage.class));
                });
    }

    @Test
    void end_to_end_query_request_response() {
        // Use mock CQClient to verify query request-response through KubeMQTemplate
        CQClient mockCq = mock(CQClient.class);
        QueryResponseMessage expectedResp = QueryResponseMessage.builder()
                .isExecuted(true).build();
        when(mockCq.sendQueryRequest(any(QueryMessage.class))).thenReturn(expectedResp);

        contextRunner
                .withBean("kubemqCQClient", CQClient.class, () -> mockCq)
                .run(context -> {
                    KubeMQTemplate template = context.getBean(KubeMQTemplate.class);
                    QueryResponseMessage response = template.sendQuery(
                            "e2e-query-ch", "query-data", Duration.ofSeconds(10));

                    assertThat(response).isSameAs(expectedResp);
                    assertThat(response.isExecuted()).isTrue();
                    verify(mockCq).sendQueryRequest(any(QueryMessage.class));
                });
    }

    @Test
    void end_to_end_queue_send_and_poll() {
        // Use mock QueuesClient to verify queue message send through KubeMQTemplate
        QueuesClient mockQueues = mock(QueuesClient.class);

        contextRunner
                .withBean("kubemqQueuesClient", QueuesClient.class, () -> mockQueues)
                .run(context -> {
                    KubeMQTemplate template = context.getBean(KubeMQTemplate.class);
                    template.sendQueueMessage("e2e-queue-ch", "queue-payload");

                    ArgumentCaptor<QueueMessage> captor = ArgumentCaptor.forClass(QueueMessage.class);
                    verify(mockQueues).sendQueueMessage(captor.capture());
                    assertThat(captor.getValue().getChannel()).isEqualTo("e2e-queue-ch");
                    assertThat(captor.getValue().getBody()).isNotEmpty();
                });
    }
}
