package io.kubemq.spring.boot.test;

/**
 * Determines the testing strategy for KubeMQ integration tests.
 *
 * <ul>
 *   <li>{@link #MOCK} -- gRPC InProcess server, no Docker, sub-100ms startup</li>
 *   <li>{@link #EMBEDDED} -- TestContainers with real KubeMQ broker</li>
 *   <li>{@link #EXTERNAL} -- Pre-existing broker (staging/CI)</li>
 * </ul>
 */
public enum KubeMQTestMode {

    /**
     * Uses a gRPC InProcess mock server.
     * No Docker required, sub-100ms startup time.
     * Best for unit tests: template logic, serialization, error handling.
     */
    MOCK,

    /**
     * Uses TestContainers with a real KubeMQ community container.
     * Requires Docker, ~5-10s startup time.
     * Best for integration tests covering all 5 messaging patterns.
     */
    EMBEDDED,

    /**
     * Connects to an external pre-existing KubeMQ broker.
     * No container management, zero startup overhead.
     * Best for staging/CI environments with dedicated brokers.
     */
    EXTERNAL
}
