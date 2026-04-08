package io.kubemq.spring.boot.test;

import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties;
import org.awaitility.Awaitility;

import java.net.Socket;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/**
 * Static helper utilities for KubeMQ tests.
 *
 * <p>Provides methods for property configuration, broker readiness checks,
 * and unique channel name generation.
 */
public final class KubeMQTestUtils {

    private KubeMQTestUtils() {
        throw new AssertionError("Utility class");
    }

    /**
     * Creates a {@link KubeMQProperties} pre-configured for the given address.
     *
     * @param address the KubeMQ broker address in {@code host:port} format
     * @return configured properties instance
     */
    public static KubeMQProperties clientProperties(String address) {
        KubeMQProperties properties = new KubeMQProperties();
        properties.setAddress(address);
        properties.setClientId("test-client-" + UUID.randomUUID().toString().substring(0, 8));
        return properties;
    }

    /**
     * Creates a {@link KubeMQProperties} pre-configured for the given
     * {@link KubeMQContainer}.
     *
     * @param container a running KubeMQ TestContainer
     * @return configured properties instance
     */
    public static KubeMQProperties clientProperties(KubeMQContainer container) {
        return clientProperties(container.getGrpcAddress());
    }

    /**
     * Blocks until the KubeMQ broker at the given address is accepting gRPC connections.
     *
     * @param address broker address in {@code host:port} format
     * @param timeout maximum time to wait
     * @throws org.awaitility.core.ConditionTimeoutException if broker is not ready within timeout
     */
    public static void waitForBrokerReady(String address, Duration timeout) {
        Objects.requireNonNull(address, "address must not be null");
        int lastColon = address.lastIndexOf(':');
        if (lastColon <= 0 || lastColon == address.length() - 1) {
            throw new IllegalArgumentException(
                    "Invalid address format '" + address + "'. Expected 'host:port'.");
        }
        String host = address.substring(0, lastColon);
        int port;
        try {
            port = Integer.parseInt(address.substring(lastColon + 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port in address '" + address + "'", e);
        }
        Awaitility.await()
                .atMost(timeout)
                .pollInterval(Duration.ofMillis(500))
                .ignoreExceptions()
                .until(() -> {
                    try (Socket s = new Socket(host, port)) {
                        return s.isConnected();
                    }
                });
    }

    /**
     * Blocks until the KubeMQ broker in the given container is accepting connections.
     *
     * @param container a running KubeMQ TestContainer
     * @param timeout   maximum time to wait
     */
    public static void waitForBrokerReady(KubeMQContainer container, Duration timeout) {
        waitForBrokerReady(container.getGrpcAddress(), timeout);
    }

    /**
     * Generates a unique channel name for test isolation.
     *
     * @param prefix the channel name prefix
     * @return a unique channel name like {@code "test-events-a1b2c3d4"}
     */
    public static String uniqueChannel(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generates a unique channel name with the default {@code "test"} prefix.
     */
    public static String uniqueChannel() {
        return uniqueChannel("test");
    }
}
