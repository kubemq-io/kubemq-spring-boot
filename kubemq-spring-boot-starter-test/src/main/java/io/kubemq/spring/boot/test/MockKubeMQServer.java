package io.kubemq.spring.boot.test;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;

import java.io.Closeable;
import java.io.IOException;

/**
 * Wraps a gRPC InProcess server with a {@link MockKubeMQService} for fast,
 * Docker-free unit testing of KubeMQ Spring Boot components.
 *
 * <p>The server uses a unique in-process name per instance, preventing
 * cross-test interference. Both the server and channel use direct executors
 * for deterministic test behavior.
 *
 * <p>Usage:
 * <pre>{@code
 * MockKubeMQServer mockServer = new MockKubeMQServer();
 * mockServer.start();
 * try {
 *     ManagedChannel channel = mockServer.getChannel();
 *     // use channel for testing...
 *     mockServer.getMockService().getReceivedEvents();
 * } finally {
 *     mockServer.close();
 * }
 * }</pre>
 */
public class MockKubeMQServer implements Closeable {

    private final String serverName = InProcessServerBuilder.generateName();
    private Server server;
    private ManagedChannel channel;
    private final MockKubeMQService mockService = new MockKubeMQService();

    /**
     * Starts the in-process gRPC server and creates a channel to it.
     *
     * @throws IOException if the server fails to start
     */
    public void start() throws IOException {
        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(mockService)
                .build()
                .start();
        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();
    }

    /**
     * Returns the gRPC channel connected to this mock server.
     *
     * @throws IllegalStateException if the server has not been started
     */
    public ManagedChannel getChannel() {
        if (channel == null) {
            throw new IllegalStateException("MockKubeMQServer has not been started");
        }
        return channel;
    }

    /**
     * Returns the mock service for configuring responses and inspecting captured messages.
     */
    public MockKubeMQService getMockService() {
        return mockService;
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.shutdownNow();
            try {
                channel.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (server != null) {
            server.shutdownNow();
            try {
                server.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
