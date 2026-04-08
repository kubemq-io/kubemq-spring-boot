package io.kubemq.spring.boot.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextCustomizer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KubeMQTestContextCustomizerFactoryTest {

    private KubeMQTestContextCustomizerFactory factory;

    @BeforeEach
    void setUp() {
        factory = new KubeMQTestContextCustomizerFactory();
    }

    @KubeMQTest
    static class AnnotatedWithKubeMQTest {}

    @EmbeddedKubeMQ(image = "kubemq/kubemq-community:v2.0.0", reuse = true)
    static class AnnotatedWithEmbeddedKubeMQ {}

    static class NoAnnotation {}

    @Test
    void createContextCustomizer_withKubeMQTestAnnotation_returnsMockCustomizer() {
        ContextCustomizer customizer = factory.createContextCustomizer(
                AnnotatedWithKubeMQTest.class, List.of());

        assertThat(customizer).isNotNull();
        // Should create a MOCK mode customizer
        assertThat(customizer).isEqualTo(
                new KubeMQTestContextCustomizer(KubeMQTestMode.MOCK, null, false));
    }

    @Test
    void createContextCustomizer_withEmbeddedKubeMQAnnotation_returnsEmbeddedCustomizer() {
        ContextCustomizer customizer = factory.createContextCustomizer(
                AnnotatedWithEmbeddedKubeMQ.class, List.of());

        assertThat(customizer).isNotNull();
        assertThat(customizer).isEqualTo(
                new KubeMQTestContextCustomizer(KubeMQTestMode.EMBEDDED,
                        "kubemq/kubemq-community:v2.0.0", true));
    }

    @Test
    void createContextCustomizer_noAnnotation_returnsNull() {
        ContextCustomizer customizer = factory.createContextCustomizer(
                NoAnnotation.class, List.of());

        assertThat(customizer).isNull();
    }
}
