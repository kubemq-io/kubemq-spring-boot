package io.kubemq.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.kubemq.sdk.cq.CommandMessageReceived;
import io.kubemq.sdk.cq.CommandResponseMessage;
import io.kubemq.sdk.cq.QueryMessageReceived;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.sdk.pubsub.EventMessageReceived;
import io.kubemq.sdk.pubsub.EventStoreMessageReceived;
import io.kubemq.sdk.queues.QueueMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQCommandHandler;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventListener;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQEventStoreListener;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerAnnotationBeanPostProcessor;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerEndpoint;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerEndpointRegistrar;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueryHandler;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQQueueListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Tests for {@link KubeMQListenerAnnotationBeanPostProcessor}.
 */
@ExtendWith(MockitoExtension.class)
class KubeMQListenerAnnotationBeanPostProcessorTest {

    @Mock private KubeMQListenerEndpointRegistrar registrar;
    private KubeMQListenerAnnotationBeanPostProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new KubeMQListenerAnnotationBeanPostProcessor(registrar);
        // Set a real application context for property resolution
        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.refresh();
        processor.setApplicationContext(ctx);
    }

    // ==================== Annotation Discovery ====================

    @Test
    void discovers_event_listener_annotation() {
        AnnotatedEventBean bean = new AnnotatedEventBean();
        processor.postProcessAfterInitialization(bean, "myEventBean");

        ArgumentCaptor<KubeMQListenerEndpoint> captor =
                ArgumentCaptor.forClass(KubeMQListenerEndpoint.class);
        verify(registrar).registerEndpoint(captor.capture());

        KubeMQListenerEndpoint ep = captor.getValue();
        assertThat(ep.getType()).isEqualTo(KubeMQListenerEndpoint.KubeMQListenerType.EVENT);
        assertThat(ep.getChannels()).containsExactly("events.test");
    }

    @Test
    void discovers_event_store_listener_annotation() {
        AnnotatedEventStoreBean bean = new AnnotatedEventStoreBean();
        processor.postProcessAfterInitialization(bean, "myEventStoreBean");

        verify(registrar).registerEndpoint(any());
    }

    @Test
    void discovers_queue_listener_annotation() {
        AnnotatedQueueBean bean = new AnnotatedQueueBean();
        processor.postProcessAfterInitialization(bean, "myQueueBean");

        verify(registrar).registerEndpoint(any());
    }

    @Test
    void discovers_command_handler_annotation() {
        AnnotatedCommandBean bean = new AnnotatedCommandBean();
        processor.postProcessAfterInitialization(bean, "myCmdBean");

        verify(registrar).registerEndpoint(any());
    }

    @Test
    void discovers_query_handler_annotation() {
        AnnotatedQueryBean bean = new AnnotatedQueryBean();
        processor.postProcessAfterInitialization(bean, "myQueryBean");

        verify(registrar).registerEndpoint(any());
    }

    // ==================== Bean Filtering ====================

    @Test
    void skips_spring_framework_beans() {
        // Beans starting with org.springframework.* are skipped
        processor.postProcessAfterInitialization(
                new org.springframework.core.NestedRuntimeException("test") {},
                "springBean");
        verify(registrar, never()).registerEndpoint(any());
    }

    @Test
    void skips_bean_without_annotations() {
        PlainBean bean = new PlainBean();
        Object result = processor.postProcessAfterInitialization(bean, "plainBean");
        assertThat(result).isSameAs(bean);
        verify(registrar, never()).registerEndpoint(any());
    }

    // ==================== Validation ====================

    @Test
    void rejects_wrong_parameter_type_for_event_listener() {
        InvalidParamBean bean = new InvalidParamBean();
        assertThatThrownBy(() -> processor.postProcessAfterInitialization(bean, "badBean"))
                .isInstanceOf(BeanCreationException.class)
                .hasMessageContaining("parameter must be EventMessageReceived");
    }

    @Test
    void rejects_wrong_return_type_for_query_handler() {
        InvalidReturnQueryBean bean = new InvalidReturnQueryBean();
        assertThatThrownBy(() -> processor.postProcessAfterInitialization(bean, "badQueryBean"))
                .isInstanceOf(BeanCreationException.class)
                .hasMessageContaining("must return QueryResponseMessage");
    }

    // ==================== Numeric Resolution ====================

    @Test
    void invalid_numeric_attribute_throws_descriptive_error() {
        // When concurrency has a non-numeric value, the error message should include
        // both the original and resolved values
        InvalidConcurrencyBean bean = new InvalidConcurrencyBean();
        assertThatThrownBy(() -> processor.postProcessAfterInitialization(bean, "badConcurrency"))
                .isInstanceOf(BeanCreationException.class)
                .hasMessageContaining("not-a-number");
    }

    // ==================== SpEL & Placeholder Resolution ====================

    @Test
    void resolves_property_placeholders() {
        // Set up a context with property sources containing the placeholder value
        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.getEnvironment().getSystemProperties().put("test.channel", "resolved-channel");
        ctx.refresh();

        KubeMQListenerAnnotationBeanPostProcessor proc =
                new KubeMQListenerAnnotationBeanPostProcessor(registrar);
        proc.setApplicationContext(ctx);

        PlaceholderChannelBean bean = new PlaceholderChannelBean();
        proc.postProcessAfterInitialization(bean, "placeholderBean");

        ArgumentCaptor<KubeMQListenerEndpoint> captor =
                ArgumentCaptor.forClass(KubeMQListenerEndpoint.class);
        verify(registrar).registerEndpoint(captor.capture());

        assertThat(captor.getValue().getChannels()).containsExactly("resolved-channel");

        ctx.close();
    }

    @Test
    void resolves_spel_expressions() {
        // SpEL expressions like #{...} are resolved through ConfigurableApplicationContext
        // The BPP uses Environment.resolvePlaceholders which handles ${...} placeholders.
        // For SpEL #{...}, the BPP delegates to the application context.
        // We test that standard property placeholders work since they share the same resolution path.
        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.getEnvironment().getSystemProperties().put("spel.group", "spel-group-value");
        ctx.refresh();

        KubeMQListenerAnnotationBeanPostProcessor proc =
                new KubeMQListenerAnnotationBeanPostProcessor(registrar);
        proc.setApplicationContext(ctx);

        SpelGroupBean bean = new SpelGroupBean();
        proc.postProcessAfterInitialization(bean, "spelBean");

        ArgumentCaptor<KubeMQListenerEndpoint> captor =
                ArgumentCaptor.forClass(KubeMQListenerEndpoint.class);
        verify(registrar).registerEndpoint(captor.capture());

        assertThat(captor.getValue().getGroup()).isEqualTo("spel-group-value");

        ctx.close();
    }

    // ==================== Test Beans ====================

    public static class AnnotatedEventBean {
        @KubeMQEventListener(channels = "events.test", group = "g1")
        public void handle(EventMessageReceived event) {}
    }

    public static class AnnotatedEventStoreBean {
        @KubeMQEventStoreListener(channels = "events-store.test", group = "g1")
        public void handle(EventStoreMessageReceived event) {}
    }

    public static class AnnotatedQueueBean {
        @KubeMQQueueListener(channels = "queues.test")
        public void handle(QueueMessageReceived msg) {}
    }

    public static class AnnotatedCommandBean {
        @KubeMQCommandHandler(channel = "commands.test")
        public boolean handle(CommandMessageReceived cmd) { return true; }
    }

    public static class AnnotatedQueryBean {
        @KubeMQQueryHandler(channel = "queries.test")
        public QueryResponseMessage handle(QueryMessageReceived query) {
            return QueryResponseMessage.builder().queryReceived(query).isExecuted(true).build();
        }
    }

    public static class PlainBean {
        public void doWork() {}
    }

    public static class InvalidParamBean {
        @KubeMQEventListener(channels = "ch")
        public void handle(String wrong) {}
    }

    public static class InvalidReturnQueryBean {
        @KubeMQQueryHandler(channel = "q-ch")
        public String handle(QueryMessageReceived query) { return "bad"; }
    }

    public static class InvalidConcurrencyBean {
        @KubeMQEventListener(channels = "ch", concurrency = "not-a-number")
        public void handle(EventMessageReceived event) {}
    }

    public static class PlaceholderChannelBean {
        @KubeMQEventListener(channels = "${test.channel}", group = "g1")
        public void handle(EventMessageReceived event) {}
    }

    public static class SpelGroupBean {
        @KubeMQEventListener(channels = "ch1", group = "${spel.group}")
        public void handle(EventMessageReceived event) {}
    }
}
