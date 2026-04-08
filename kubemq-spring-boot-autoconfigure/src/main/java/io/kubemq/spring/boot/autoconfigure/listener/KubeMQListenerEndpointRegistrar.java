package io.kubemq.spring.boot.autoconfigure.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.ErrorHandler;

/**
 * Registry that holds all discovered {@link KubeMQListenerEndpoint} instances and their
 * associated {@link KubeMQMessageListenerContainer} instances.
 *
 * <p>Endpoints are registered during bean post-processing; containers are created and started
 * via the {@link SmartLifecycle} callback after all singletons are initialized.
 *
 * <p>Implements {@link SmartLifecycle} so Spring manages container startup/shutdown ordering,
 * and {@link DisposableBean} for cleanup on context close.
 */
public class KubeMQListenerEndpointRegistrar implements SmartLifecycle, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(KubeMQListenerEndpointRegistrar.class);

    private final KubeMQListenerContainerFactory defaultContainerFactory;
    private final ConfigurableListableBeanFactory beanFactory;
    private final List<KubeMQListenerEndpoint> endpoints = new ArrayList<>();
    private final Map<String, KubeMQMessageListenerContainer> containers = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public KubeMQListenerEndpointRegistrar(
            KubeMQListenerContainerFactory defaultContainerFactory,
            ConfigurableListableBeanFactory beanFactory) {
        this.defaultContainerFactory = defaultContainerFactory;
        this.beanFactory = beanFactory;
    }

    /**
     * Registers a listener endpoint. Must be called before {@link #start()}.
     */
    public synchronized void registerEndpoint(KubeMQListenerEndpoint endpoint) {
        if (running.get()) {
            throw new IllegalStateException(
                    "Cannot register endpoints after containers have been started. "
                            + "Endpoint: " + endpoint.getId());
        }
        log.debug("Registering KubeMQ listener endpoint '{}' [type={}]",
                endpoint.getId(), endpoint.getType());
        endpoints.add(endpoint);
    }

    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        for (KubeMQListenerEndpoint endpoint : endpoints) {
            KubeMQListenerContainerFactory factory = resolveContainerFactory(endpoint);
            ErrorHandler errorHandler = resolveErrorHandler(endpoint);
            KubeMQMessageListenerContainer container = factory.createContainer(endpoint, errorHandler);
            containers.put(endpoint.getId(), container);
            beanFactory.registerSingleton("kubemqListenerContainer-" + endpoint.getId(), container);
            if (endpoint.isAutoStartup()) {
                log.info("Starting KubeMQ listener container '{}'", endpoint.getId());
                container.start();
            } else {
                log.info("KubeMQ listener container '{}' registered but auto-startup disabled",
                        endpoint.getId());
            }
        }
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        for (Map.Entry<String, KubeMQMessageListenerContainer> entry : containers.entrySet()) {
            if (entry.getValue().isRunning()) {
                log.info("Stopping KubeMQ listener container '{}'", entry.getKey());
                entry.getValue().stop();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 50;
    }

    @Override
    public void destroy() {
        stop();
    }

    /**
     * Resolves a custom container factory by bean name if specified on the endpoint,
     * otherwise falls back to the default factory.
     */
    private KubeMQListenerContainerFactory resolveContainerFactory(KubeMQListenerEndpoint endpoint) {
        String factoryBeanName = endpoint.getContainerFactoryBeanName();
        if (factoryBeanName != null && !factoryBeanName.isEmpty()) {
            return beanFactory.getBean(factoryBeanName, KubeMQListenerContainerFactory.class);
        }
        return defaultContainerFactory;
    }

    /**
     * Resolves a custom error handler by bean name if specified on the endpoint,
     * otherwise returns null (factory will use its default).
     */
    private ErrorHandler resolveErrorHandler(KubeMQListenerEndpoint endpoint) {
        String handlerBeanName = endpoint.getErrorHandlerBeanName();
        if (handlerBeanName != null && !handlerBeanName.isEmpty()) {
            return beanFactory.getBean(handlerBeanName, ErrorHandler.class);
        }
        return null;
    }

    /**
     * Returns an unmodifiable view of registered endpoints.
     */
    public List<KubeMQListenerEndpoint> getEndpoints() {
        return Collections.unmodifiableList(endpoints);
    }

    /**
     * Returns the container associated with the given endpoint ID, or {@code null}.
     */
    public KubeMQMessageListenerContainer getContainer(String endpointId) {
        return containers.get(endpointId);
    }

    /**
     * Returns an unmodifiable view of all containers.
     */
    public Map<String, KubeMQMessageListenerContainer> getContainers() {
        return Collections.unmodifiableMap(containers);
    }
}
