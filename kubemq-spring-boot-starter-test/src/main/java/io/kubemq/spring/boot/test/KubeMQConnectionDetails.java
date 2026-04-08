package io.kubemq.spring.boot.test;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

/**
 * Spring Boot {@link ConnectionDetails} interface for KubeMQ connections.
 *
 * <p>Provides the gRPC address for connecting to a KubeMQ broker.
 * Implementations include container-based (TestContainers) and external broker details.
 *
 * @see KubeMQContainerConnectionDetailsFactory
 */
public interface KubeMQConnectionDetails extends ConnectionDetails {

    /**
     * Returns the gRPC address in {@code host:port} format.
     */
    String getAddress();
}
