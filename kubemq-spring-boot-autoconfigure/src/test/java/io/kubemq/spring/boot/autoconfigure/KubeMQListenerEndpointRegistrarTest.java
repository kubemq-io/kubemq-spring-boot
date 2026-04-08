package io.kubemq.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.pubsub.EventMessageReceived;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerContainerFactory;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerEndpoint;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerEndpoint.KubeMQListenerType;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerEndpointRegistrar;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQMessageListenerContainer;
import io.kubemq.spring.boot.autoconfigure.listener.MethodKubeMQListenerEndpoint;
import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import java.lang.reflect.Method;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ErrorHandler;

/**
 * Tests for {@link KubeMQListenerEndpointRegistrar}.
 */
@ExtendWith(MockitoExtension.class)
class KubeMQListenerEndpointRegistrarTest {

    @Mock private KubeMQListenerContainerFactory defaultFactory;
    @Mock private ConfigurableListableBeanFactory beanFactory;

    private KubeMQListenerEndpointRegistrar registrar;
    private MethodKubeMQListenerEndpoint sampleEndpoint;

    @BeforeEach
    void setUp() throws Exception {
        registrar = new KubeMQListenerEndpointRegistrar(defaultFactory, beanFactory);

        Object bean = new TestBean();
        Method method = TestBean.class.getMethod("handleEvent", EventMessageReceived.class);
        sampleEndpoint = MethodKubeMQListenerEndpoint.builder()
                .id("ep-1").type(KubeMQListenerType.EVENT)
                .channels(new String[]{"ch1"}).group("grp")
                .bean(bean).method(method).autoStartup(true).build();
    }

    @Test
    void registerEndpoint_adds_to_list() {
        registrar.registerEndpoint(sampleEndpoint);
        assertThat(registrar.getEndpoints()).containsExactly(sampleEndpoint);
    }

    @Test
    void registerEndpoint_after_start_throws() {
        // Stub factory to return a mock container
        KubeMQMessageListenerContainer mockContainer = mock(KubeMQMessageListenerContainer.class);
        when(defaultFactory.createContainer(any(), any())).thenReturn(mockContainer);

        registrar.registerEndpoint(sampleEndpoint);
        registrar.start();

        assertThatThrownBy(() -> registrar.registerEndpoint(sampleEndpoint))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot register endpoints after containers have been started");
    }

    @Test
    void start_creates_and_starts_containers() {
        KubeMQMessageListenerContainer mockContainer = mock(KubeMQMessageListenerContainer.class);
        when(defaultFactory.createContainer(any(), any())).thenReturn(mockContainer);

        registrar.registerEndpoint(sampleEndpoint);
        registrar.start();

        assertThat(registrar.isRunning()).isTrue();
        assertThat(registrar.getContainer("ep-1")).isSameAs(mockContainer);
        verify(mockContainer).start();
    }

    @Test
    void start_does_not_start_container_when_autoStartup_false() throws Exception {
        Object bean = new TestBean();
        Method method = TestBean.class.getMethod("handleEvent", EventMessageReceived.class);
        MethodKubeMQListenerEndpoint noAutoEp = MethodKubeMQListenerEndpoint.builder()
                .id("ep-noauto").type(KubeMQListenerType.EVENT)
                .channels(new String[]{"ch1"}).group("grp")
                .bean(bean).method(method).autoStartup(false).build();

        KubeMQMessageListenerContainer mockContainer = mock(KubeMQMessageListenerContainer.class);
        when(defaultFactory.createContainer(any(), any())).thenReturn(mockContainer);

        registrar.registerEndpoint(noAutoEp);
        registrar.start();

        verify(mockContainer, never()).start();
        assertThat(registrar.getContainer("ep-noauto")).isSameAs(mockContainer);
    }

    @Test
    void stop_stops_running_containers() {
        KubeMQMessageListenerContainer mockContainer = mock(KubeMQMessageListenerContainer.class);
        when(defaultFactory.createContainer(any(), any())).thenReturn(mockContainer);
        when(mockContainer.isRunning()).thenReturn(true);

        registrar.registerEndpoint(sampleEndpoint);
        registrar.start();
        registrar.stop();

        assertThat(registrar.isRunning()).isFalse();
        verify(mockContainer).stop();
    }

    @Test
    void destroy_delegates_to_stop() {
        KubeMQMessageListenerContainer mockContainer = mock(KubeMQMessageListenerContainer.class);
        when(defaultFactory.createContainer(any(), any())).thenReturn(mockContainer);
        when(mockContainer.isRunning()).thenReturn(true);

        registrar.registerEndpoint(sampleEndpoint);
        registrar.start();
        registrar.destroy();

        assertThat(registrar.isRunning()).isFalse();
    }

    @Test
    void getPhase_returns_expected_value() {
        assertThat(registrar.getPhase()).isEqualTo(Integer.MAX_VALUE - 50);
    }

    public static class TestBean {
        public void handleEvent(EventMessageReceived event) {}
    }
}
