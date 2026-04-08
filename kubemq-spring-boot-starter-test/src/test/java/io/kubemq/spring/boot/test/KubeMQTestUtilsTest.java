package io.kubemq.spring.boot.test;

import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KubeMQTestUtilsTest {

    @Test
    void clientProperties_validAddress_createsConfiguredProperties() {
        KubeMQProperties props = KubeMQTestUtils.clientProperties("localhost:50000");

        assertThat(props.getAddress()).isEqualTo("localhost:50000");
        assertThat(props.getClientId()).isNotBlank();
        assertThat(props.getClientId()).startsWith("test-client-");
    }

    @Test
    void waitForBrokerReady_invalidAddressFormat_throwsIllegalArgumentException() {
        assertThatThrownBy(() ->
                KubeMQTestUtils.waitForBrokerReady("invalid-no-port",
                        java.time.Duration.ofSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid address format");
    }

    @Test
    void waitForBrokerReady_nullAddress_throwsNullPointerException() {
        assertThatThrownBy(() ->
                KubeMQTestUtils.waitForBrokerReady((String) null,
                        java.time.Duration.ofSeconds(1)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void uniqueChannel_generatesUniqueNames() {
        String ch1 = KubeMQTestUtils.uniqueChannel("events");
        String ch2 = KubeMQTestUtils.uniqueChannel("events");

        assertThat(ch1).startsWith("events-");
        assertThat(ch2).startsWith("events-");
        assertThat(ch1).isNotEqualTo(ch2);
    }

    @Test
    void uniqueChannel_defaultPrefix_usesTest() {
        String ch = KubeMQTestUtils.uniqueChannel();
        assertThat(ch).startsWith("test-");
    }

    @Test
    void ipv6_address_parsed() {
        // IPv6 address format [::1]:port should be parsed correctly.
        // waitForBrokerReady uses lastIndexOf(':') to split host and port,
        // so [::1]:59999 -> host="[::1]", port=59999.
        // Use a port unlikely to be in use so the call times out rather than connects.
        // The call should fail with ConditionTimeoutException (no broker),
        // NOT with IllegalArgumentException from address parsing.
        assertThatThrownBy(() ->
                KubeMQTestUtils.waitForBrokerReady("[::1]:59999",
                        java.time.Duration.ofSeconds(2)))
                .isNotInstanceOf(IllegalArgumentException.class);
    }
}
