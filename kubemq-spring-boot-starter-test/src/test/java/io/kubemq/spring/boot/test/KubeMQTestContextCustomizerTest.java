package io.kubemq.spring.boot.test;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KubeMQTestContextCustomizerTest {

    @Test
    void mockMode_equalityBasedOnModeAndProperties() {
        KubeMQTestContextCustomizer c1 = new KubeMQTestContextCustomizer(
                KubeMQTestMode.MOCK, null, false);
        KubeMQTestContextCustomizer c2 = new KubeMQTestContextCustomizer(
                KubeMQTestMode.MOCK, null, false);

        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    void embeddedMode_differentImageMeansNotEqual() {
        KubeMQTestContextCustomizer c1 = new KubeMQTestContextCustomizer(
                KubeMQTestMode.EMBEDDED, "kubemq/kubemq-community:v1.0.0", false);
        KubeMQTestContextCustomizer c2 = new KubeMQTestContextCustomizer(
                KubeMQTestMode.EMBEDDED, "kubemq/kubemq-community:v2.0.0", false);

        assertThat(c1).isNotEqualTo(c2);
    }

    @Test
    void reuseFlag_affectsEquality() {
        KubeMQTestContextCustomizer c1 = new KubeMQTestContextCustomizer(
                KubeMQTestMode.EMBEDDED, "kubemq/kubemq-community:latest", false);
        KubeMQTestContextCustomizer c2 = new KubeMQTestContextCustomizer(
                KubeMQTestMode.EMBEDDED, "kubemq/kubemq-community:latest", true);

        assertThat(c1).isNotEqualTo(c2);
    }
}
