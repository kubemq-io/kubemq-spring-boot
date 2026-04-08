package io.kubemq.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.pubsub.EventMessageReceived;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueueMessageReceived;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerContainerFactory;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerEndpoint.KubeMQListenerType;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQMessageListenerContainer;
import io.kubemq.spring.boot.autoconfigure.listener.MethodKubeMQListenerEndpoint;
import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ErrorHandler;

/**
 * Tests for {@link KubeMQListenerContainerFactory}.
 */
@ExtendWith(MockitoExtension.class)
class KubeMQListenerContainerFactoryTest {

    @Mock private PubSubClient pubSubClient;
    @Mock private QueuesClient queuesClient;
    @Mock private CQClient cqClient;
    @Mock private ErrorHandler errorHandler;
    @Mock private KubeMQMessageConverter messageConverter;

    private KubeMQProperties properties;
    private KubeMQListenerContainerFactory factory;
    private Object listenerBean;
    private Method eventMethod;

    @BeforeEach
    void setUp() throws Exception {
        properties = new KubeMQProperties();
        factory = new KubeMQListenerContainerFactory(
                pubSubClient, queuesClient, cqClient,
                properties, errorHandler, messageConverter, null);
        listenerBean = new TestListenerBean();
        eventMethod = TestListenerBean.class.getMethod("handleEvent", EventMessageReceived.class);
    }

    @Test
    void createContainer_returns_configured_container() {
        MethodKubeMQListenerEndpoint endpoint = MethodKubeMQListenerEndpoint.builder()
                .id("ep-1").type(KubeMQListenerType.EVENT)
                .channels(new String[]{"ch1"}).group("grp")
                .concurrency(2).bean(listenerBean).method(eventMethod).build();

        KubeMQMessageListenerContainer container = factory.createContainer(endpoint);

        assertThat(container).isNotNull();
        assertThat(container.getId()).isEqualTo("ep-1");
        assertThat(container.getType()).isEqualTo(KubeMQListenerType.EVENT);
    }

    @Test
    void createContainer_applies_default_concurrency_from_properties() {
        properties.getListener().setConcurrency(8);

        // Endpoint with concurrency=0 means use default
        MethodKubeMQListenerEndpoint endpoint = MethodKubeMQListenerEndpoint.builder()
                .id("ep-2").type(KubeMQListenerType.EVENT)
                .channels(new String[]{"ch1"}).group("grp")
                .concurrency(0).bean(listenerBean).method(eventMethod).build();

        KubeMQMessageListenerContainer container = factory.createContainer(endpoint);
        // The container's effective concurrency is resolved from endpoint which now has 8
        assertThat(container.getConcurrency()).isEqualTo(8);
    }

    @Test
    void createContainer_with_errorHandler_override() {
        ErrorHandler customHandler = t -> {};

        MethodKubeMQListenerEndpoint endpoint = MethodKubeMQListenerEndpoint.builder()
                .id("ep-3").type(KubeMQListenerType.EVENT)
                .channels(new String[]{"ch1"}).group("grp")
                .bean(listenerBean).method(eventMethod).build();

        KubeMQMessageListenerContainer container = factory.createContainer(endpoint, customHandler);
        assertThat(container).isNotNull();
    }

    @Test
    void createContainer_applies_queue_defaults_from_properties() throws Exception {
        properties.getListener().getQueues().setMaxPollMessages(10);

        Method queueMethod = TestListenerBean.class.getMethod("handleQueue", QueueMessageReceived.class);
        MethodKubeMQListenerEndpoint endpoint = MethodKubeMQListenerEndpoint.builder()
                .id("ep-q").type(KubeMQListenerType.QUEUE)
                .channels(new String[]{"q1"}).group("")
                .bean(listenerBean).method(queueMethod).build();

        KubeMQMessageListenerContainer container = factory.createContainer(endpoint);
        assertThat(container).isNotNull();
        assertThat(container.getType()).isEqualTo(KubeMQListenerType.QUEUE);
    }

    @Test
    void createContainer_respects_global_autoStartup_false() {
        properties.getListener().setAutoStartup(false);

        MethodKubeMQListenerEndpoint endpoint = MethodKubeMQListenerEndpoint.builder()
                .id("ep-nostart").type(KubeMQListenerType.EVENT)
                .channels(new String[]{"ch1"}).group("grp")
                .autoStartup(true) // per-annotation says true, but global override is false
                .bean(listenerBean).method(eventMethod).build();

        KubeMQMessageListenerContainer container = factory.createContainer(endpoint);
        assertThat(container.isAutoStartup()).isFalse();
    }

    @Test
    void createContainer_rejects_non_method_endpoint() {
        KubeMQListenerContainerFactory f = new KubeMQListenerContainerFactory(
                pubSubClient, queuesClient, cqClient,
                properties, errorHandler, messageConverter, null);

        // Create a non-MethodKubeMQListenerEndpoint
        var endpoint = new io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerEndpoint() {
            @Override public String getId() { return "x"; }
            @Override public KubeMQListenerType getType() { return KubeMQListenerType.EVENT; }
            @Override public String[] getChannels() { return new String[]{"c"}; }
            @Override public String getGroup() { return ""; }
            @Override public int getConcurrency() { return 1; }
            @Override public boolean isAutoStartup() { return true; }
            @Override public Object getBean() { return listenerBean; }
            @Override public Method getMethod() { return eventMethod; }
            @Override public String getErrorHandlerBeanName() { return ""; }
            @Override public String getContainerFactoryBeanName() { return ""; }
        };

        assertThatThrownBy(() -> f.createContainer(endpoint))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported endpoint type");
    }

    public static class TestListenerBean {
        public void handleEvent(EventMessageReceived event) {}
        public void handleQueue(QueueMessageReceived msg) {}
    }
}
