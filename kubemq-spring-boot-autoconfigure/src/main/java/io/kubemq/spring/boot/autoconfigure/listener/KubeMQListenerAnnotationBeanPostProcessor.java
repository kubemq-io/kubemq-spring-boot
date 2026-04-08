package io.kubemq.spring.boot.autoconfigure.listener;

import io.kubemq.sdk.cq.CommandMessageReceived;
import io.kubemq.sdk.cq.CommandResponseMessage;
import io.kubemq.sdk.cq.QueryMessageReceived;
import io.kubemq.sdk.cq.QueryResponseMessage;
import io.kubemq.sdk.pubsub.EventMessageReceived;
import io.kubemq.sdk.pubsub.EventStoreMessageReceived;
import io.kubemq.sdk.pubsub.EventsStoreType;
import io.kubemq.sdk.queues.QueueMessageReceived;
import io.kubemq.spring.boot.autoconfigure.listener.KubeMQListenerEndpoint.KubeMQListenerType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Scans Spring beans for KubeMQ listener annotations and registers discovered endpoints
 * with the {@link KubeMQListenerEndpointRegistrar}.
 *
 * <p>Implements {@link SmartInitializingSingleton} so that all containers are started
 * after all beans have been fully initialized — no ordering issues.
 */
public class KubeMQListenerAnnotationBeanPostProcessor
        implements BeanPostProcessor, SmartInitializingSingleton, ApplicationContextAware {

    private static final Logger log =
            LoggerFactory.getLogger(KubeMQListenerAnnotationBeanPostProcessor.class);

    private final KubeMQListenerEndpointRegistrar registrar;
    private final AtomicInteger endpointCounter = new AtomicInteger(0);
    private ConfigurableApplicationContext applicationContext;

    public KubeMQListenerAnnotationBeanPostProcessor(KubeMQListenerEndpointRegistrar registrar) {
        this.registrar = registrar;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof ConfigurableApplicationContext configCtx) {
            this.applicationContext = configCtx;
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        String className = targetClass.getName();
        if (className.startsWith("org.springframework.")
                || className.startsWith("org.apache.")
                || className.startsWith("io.micrometer.")
                || className.startsWith("io.grpc.")) {
            return bean;
        }
        ReflectionUtils.doWithMethods(targetClass, method -> {
            processAnnotation(bean, method, KubeMQEventListener.class, KubeMQListenerType.EVENT);
            processAnnotation(bean, method, KubeMQEventStoreListener.class, KubeMQListenerType.EVENT_STORE);
            processAnnotation(bean, method, KubeMQQueueListener.class, KubeMQListenerType.QUEUE);
            processAnnotation(bean, method, KubeMQCommandHandler.class, KubeMQListenerType.COMMAND);
            processAnnotation(bean, method, KubeMQQueryHandler.class, KubeMQListenerType.QUERY);
        });
        return bean;
    }

    @Override
    public void afterSingletonsInstantiated() {
        // Container startup is handled by the registrar's SmartLifecycle.start()
    }

    private <A extends Annotation> void processAnnotation(
            Object bean, Method method, Class<A> annotationType, KubeMQListenerType type) {

        A annotation = AnnotatedElementUtils.findMergedAnnotation(method, annotationType);
        if (annotation == null) {
            return;
        }

        Method invocableMethod = MethodIntrospector.selectInvocableMethod(method, bean.getClass());
        MethodKubeMQListenerEndpoint.Builder builder = MethodKubeMQListenerEndpoint.builder()
                .type(type)
                .bean(bean)
                .method(invocableMethod);

        if (annotation instanceof KubeMQEventListener a) {
            builder.id(resolveId(a.id(), type))
                    .channels(resolveStrings(a.channels()))
                    .group(resolveString(a.group()))
                    .concurrency(resolveInt(a.concurrency(), 0))
                    .autoStartup(resolveBool(a.autoStartup(), true))
                    .errorHandlerBeanName(resolveString(a.errorHandler()))
                    .containerFactoryBeanName(resolveString(a.containerFactory()));
        } else if (annotation instanceof KubeMQEventStoreListener a) {
            builder.id(resolveId(a.id(), type))
                    .channels(resolveStrings(a.channels()))
                    .group(resolveString(a.group()))
                    .concurrency(resolveInt(a.concurrency(), 0))
                    .autoStartup(resolveBool(a.autoStartup(), true))
                    .errorHandlerBeanName(resolveString(a.errorHandler()))
                    .containerFactoryBeanName(resolveString(a.containerFactory()))
                    .eventsStoreType(resolveEventsStoreType(a.subscriptionType()))
                    .eventsStoreValue(resolveLong(a.subscriptionValue(), 0L));
        } else if (annotation instanceof KubeMQQueueListener a) {
            builder.id(resolveId(a.id(), type))
                    .channels(resolveStrings(a.channels()))
                    .group("")
                    .concurrency(resolveInt(a.concurrency(), 0))
                    .autoStartup(resolveBool(a.autoStartup(), true))
                    .errorHandlerBeanName(resolveString(a.errorHandler()))
                    .containerFactoryBeanName(resolveString(a.containerFactory()))
                    .pollTimeoutSeconds(resolveInt(a.pollTimeout(), 0))
                    .maxPollMessages(resolveInt(a.maxPollMessages(), 0))
                    .visibilityTimeoutSeconds(resolveInt(a.visibilityTimeout(), 0))
                    .autoAck(resolveBool(a.autoAck(), false))
                    .batch(resolveBool(a.batch(), false));
        } else if (annotation instanceof KubeMQCommandHandler a) {
            String resolvedChannel = resolveString(a.channel());
            builder.id(resolveId(a.id(), type))
                    .channels(new String[]{resolvedChannel})
                    .group(resolveString(a.group()))
                    .concurrency(resolveInt(a.concurrency(), 0))
                    .autoStartup(resolveBool(a.autoStartup(), true))
                    .errorHandlerBeanName(resolveString(a.errorHandler()))
                    .containerFactoryBeanName(resolveString(a.containerFactory()));
        } else if (annotation instanceof KubeMQQueryHandler a) {
            String resolvedChannel = resolveString(a.channel());
            builder.id(resolveId(a.id(), type))
                    .channels(new String[]{resolvedChannel})
                    .group(resolveString(a.group()))
                    .concurrency(resolveInt(a.concurrency(), 0))
                    .autoStartup(resolveBool(a.autoStartup(), true))
                    .errorHandlerBeanName(resolveString(a.errorHandler()))
                    .containerFactoryBeanName(resolveString(a.containerFactory()));
        } else {
            return;
        }

        validateReturnType(invocableMethod, type);
        validateParameterType(invocableMethod, type);

        MethodKubeMQListenerEndpoint endpoint = builder.build();
        log.info("Discovered KubeMQ {} listener on {}.{} -> channels={}",
                type, bean.getClass().getSimpleName(), method.getName(), endpoint.getChannels());
        registrar.registerEndpoint(endpoint);
    }

    // ==================== SpEL / Placeholder Resolution ====================

    private String resolveString(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        if (applicationContext != null) {
            return applicationContext.getEnvironment().resolvePlaceholders(value);
        }
        return value;
    }

    private String[] resolveStrings(String[] values) {
        String[] resolved = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            resolved[i] = resolveString(values[i]);
        }
        return resolved;
    }

    private int resolveInt(String value, int defaultValue) {
        String resolved = resolveString(value);
        if (!StringUtils.hasText(resolved)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(resolved.trim());
        } catch (NumberFormatException e) {
            throw new BeanCreationException(
                    "Invalid numeric value for KubeMQ listener attribute: '" + resolved +
                    "' (original: '" + value + "'). Expected an integer.", e);
        }
    }

    private long resolveLong(String value, long defaultValue) {
        String resolved = resolveString(value);
        if (!StringUtils.hasText(resolved)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(resolved.trim());
        } catch (NumberFormatException e) {
            throw new BeanCreationException(
                    "Invalid numeric value for KubeMQ listener attribute: '" + resolved +
                    "' (original: '" + value + "'). Expected a long integer.", e);
        }
    }

    private boolean resolveBool(String value, boolean defaultValue) {
        String resolved = resolveString(value);
        if (!StringUtils.hasText(resolved)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(resolved.trim());
    }

    private String resolveId(String id, KubeMQListenerType type) {
        String resolved = resolveString(id);
        if (StringUtils.hasText(resolved)) {
            return resolved;
        }
        return "kubemq-" + type.name().toLowerCase().replace('_', '-') + "-"
                + endpointCounter.incrementAndGet();
    }

    private EventsStoreType resolveEventsStoreType(String value) {
        String resolved = resolveString(value);
        if (!StringUtils.hasText(resolved)) {
            return EventsStoreType.StartNewOnly;
        }
        try {
            return EventsStoreType.valueOf(resolved.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid EventsStoreType: '" + resolved + "'. Valid values: "
                            + Arrays.toString(EventsStoreType.values()), e);
        }
    }

    // ==================== Validation ====================

    private void validateReturnType(Method method, KubeMQListenerType type) {
        Class<?> returnType = method.getReturnType();
        switch (type) {
            case COMMAND -> {
                if (returnType != void.class && returnType != Void.class
                        && returnType != Boolean.class && returnType != boolean.class
                        && !CommandResponseMessage.class.isAssignableFrom(returnType)) {
                    throw new BeanCreationException(
                        "Command handler " + method.getDeclaringClass().getSimpleName() + "." +
                        method.getName() + " has unsupported return type: " + returnType.getName() +
                        ". Allowed: void, boolean/Boolean, CommandResponseMessage");
                }
            }
            case QUERY -> {
                if (!QueryResponseMessage.class.isAssignableFrom(returnType)) {
                    throw new BeanCreationException(
                        "Query handler " + method.getDeclaringClass().getSimpleName() + "." +
                        method.getName() + " must return QueryResponseMessage, got: " + returnType.getName());
                }
            }
            case EVENT, EVENT_STORE, QUEUE -> { /* any return type acceptable */ }
        }
    }

    private void validateParameterType(Method method, KubeMQListenerType type) {
        Class<?>[] params = method.getParameterTypes();
        if (params.length != 1) {
            if (params.length == 2 && isContinuationParameter(params[1])) {
                // Kotlin suspend function — second parameter is the continuation, OK
            } else {
                throw new BeanCreationException(
                    "KubeMQ listener " + method.getDeclaringClass().getSimpleName() + "." +
                    method.getName() + " must have exactly one parameter, got: " + params.length);
            }
        }
        Class<?> paramType = params[0];
        switch (type) {
            case EVENT -> validateAssignable(method, paramType, EventMessageReceived.class);
            case EVENT_STORE -> validateAssignable(method, paramType, EventStoreMessageReceived.class);
            case QUEUE -> {
                // Accept QueueMessageReceived or List (for batch mode)
                if (!QueueMessageReceived.class.isAssignableFrom(paramType)
                        && !List.class.isAssignableFrom(paramType)) {
                    throw new BeanCreationException(
                        "Queue listener " + method.getDeclaringClass().getSimpleName() + "." +
                        method.getName() + " parameter must be QueueMessageReceived or List, got: " +
                        paramType.getName());
                }
            }
            case COMMAND -> validateAssignable(method, paramType, CommandMessageReceived.class);
            case QUERY -> validateAssignable(method, paramType, QueryMessageReceived.class);
        }
    }

    private boolean isContinuationParameter(Class<?> param) {
        return "kotlin.coroutines.Continuation".equals(param.getName());
    }

    private void validateAssignable(Method method, Class<?> actual, Class<?> expected) {
        if (!expected.isAssignableFrom(actual)) {
            throw new BeanCreationException(
                "KubeMQ listener " + method.getDeclaringClass().getSimpleName() + "." +
                method.getName() + " parameter must be " + expected.getSimpleName() +
                ", got: " + actual.getName());
        }
    }
}
