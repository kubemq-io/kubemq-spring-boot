package io.kubemq.spring.boot.test;

import io.grpc.ManagedChannel;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Spring Test {@link ContextCustomizer} that prepares the application context
 * based on {@link KubeMQTest} or {@link EmbeddedKubeMQ} annotations.
 *
 * <p>For {@link KubeMQTestMode#MOCK}:
 * <ul>
 *   <li>Starts a {@link MockKubeMQServer} and registers it as a singleton</li>
 *   <li>Sets {@code kubemq.address} to the in-process server address</li>
 *   <li>Registers a {@link KubeMQTestHarness} singleton</li>
 * </ul>
 *
 * <p>For {@link KubeMQTestMode#EMBEDDED}:
 * <ul>
 *   <li>Starts a {@link KubeMQContainer} and registers it as a singleton</li>
 *   <li>Sets {@code kubemq.address} to the container's mapped gRPC address</li>
 * </ul>
 *
 * <p>For {@link KubeMQTestMode#EXTERNAL}: no infrastructure is started.
 */
class KubeMQTestContextCustomizer implements ContextCustomizer {

    private final KubeMQTestMode mode;
    private final String containerImage;
    private final boolean reuse;

    KubeMQTestContextCustomizer(KubeMQTestMode mode, String containerImage, boolean reuse) {
        this.mode = mode;
        this.containerImage = containerImage;
        this.reuse = reuse;
    }

    @Override
    public void customizeContext(ConfigurableApplicationContext context,
                                 MergedContextConfiguration mergedConfig) {
        switch (mode) {
            case MOCK -> configureMockMode(context);
            case EMBEDDED -> configureEmbeddedMode(context);
            case EXTERNAL -> { /* no-op */ }
        }
    }

    private void configureMockMode(ConfigurableApplicationContext context) {
        MockKubeMQServer mockServer = new MockKubeMQServer();
        try {
            mockServer.start();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start MockKubeMQServer", e);
        }

        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        beanFactory.registerSingleton("mockKubeMQServer", mockServer);
        beanFactory.registerSingleton("kubemqTestHarness",
                new KubeMQTestHarness(mockServer.getMockService()));

        // Use BeanDefinitionRegistry for typed resolution via ObjectProvider
        if (beanFactory instanceof BeanDefinitionRegistry registry) {
            RootBeanDefinition channelDef = new RootBeanDefinition(
                    ManagedChannel.class, () -> mockServer.getChannel());
            channelDef.setDestroyMethodName("shutdown");
            registry.registerBeanDefinition("kubemqTestManagedChannel", channelDef);

            registry.registerBeanDefinition("mockKubeMQService",
                    new RootBeanDefinition(MockKubeMQService.class,
                            () -> mockServer.getMockService()));
        }

        context.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                mockServer.close();
            }
        });
    }

    private void configureEmbeddedMode(ConfigurableApplicationContext context) {
        KubeMQContainer container = new KubeMQContainer(containerImage);
        container.withReuse(this.reuse);
        container.start();

        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        beanFactory.registerSingleton("kubemqContainer", container);

        context.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("kubemq-test-embedded", Map.of(
                        "kubemq.address", container.getGrpcAddress()
                ))
        );

        context.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                container.stop();
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KubeMQTestContextCustomizer that = (KubeMQTestContextCustomizer) o;
        return reuse == that.reuse && mode == that.mode
                && Objects.equals(containerImage, that.containerImage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, containerImage, reuse);
    }
}
