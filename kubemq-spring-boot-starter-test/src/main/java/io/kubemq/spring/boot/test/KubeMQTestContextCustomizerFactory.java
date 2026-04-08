package io.kubemq.spring.boot.test;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import java.util.List;

/**
 * Spring Test {@link ContextCustomizerFactory} that detects {@link KubeMQTest}
 * and {@link EmbeddedKubeMQ} annotations on test classes and creates the
 * appropriate {@link KubeMQTestContextCustomizer}.
 *
 * <p>Registered via {@code META-INF/spring.factories} or
 * {@code META-INF/spring/org.springframework.test.context.ContextCustomizerFactory.imports}.
 */
public class KubeMQTestContextCustomizerFactory implements ContextCustomizerFactory {

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass,
                                                     List<ContextConfigurationAttributes> configAttributes) {

        KubeMQTest kubemqTest = AnnotatedElementUtils.findMergedAnnotation(
                testClass, KubeMQTest.class);
        if (kubemqTest != null) {
            return new KubeMQTestContextCustomizer(KubeMQTestMode.MOCK, null, false);
        }

        EmbeddedKubeMQ embedded = AnnotatedElementUtils.findMergedAnnotation(
                testClass, EmbeddedKubeMQ.class);
        if (embedded != null) {
            return new KubeMQTestContextCustomizer(
                    KubeMQTestMode.EMBEDDED, embedded.image(), embedded.reuse());
        }

        return null;
    }
}
