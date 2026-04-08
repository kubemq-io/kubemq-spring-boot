package io.kubemq.spring.boot.test;

import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

/**
 * TestContainers wrapper for the KubeMQ community edition Docker image.
 *
 * <p>Exposes the gRPC port (50000) and REST API port (9090) with a health-check
 * wait strategy on the REST API.
 *
 * <p>Usage:
 * <pre>{@code
 * try (var kubemq = new KubeMQContainer()) {
 *     kubemq.start();
 *     String grpcAddress = kubemq.getGrpcAddress();
 * }
 * }</pre>
 */
public class KubeMQContainer extends GenericContainer<KubeMQContainer> {

    private static final DockerImageName DEFAULT_IMAGE =
            DockerImageName.parse("kubemq/kubemq-community");

    private static final String DEFAULT_TAG = "latest";

    /** System property to override the default Docker image. */
    public static final String IMAGE_PROPERTY = "kubemq.test.image";

    /** Default gRPC port exposed by the KubeMQ container. */
    public static final int GRPC_PORT = 50000;

    /** Default REST API port exposed by the KubeMQ container. */
    public static final int REST_PORT = 9090;

    /** Default dashboard/API port exposed by the KubeMQ container. */
    public static final int API_PORT = 8080;

    public KubeMQContainer() {
        this(resolveDefaultImage());
    }

    private static DockerImageName resolveDefaultImage() {
        String image = System.getProperty(IMAGE_PROPERTY);
        if (image != null && !image.isEmpty()) {
            return DockerImageName.parse(image);
        }
        LoggerFactory.getLogger(KubeMQContainer.class)
                .warn("Using KubeMQ Docker image tag 'latest'. " +
                      "For reproducible builds, set -D{}=kubemq/kubemq-community:<version>",
                      IMAGE_PROPERTY);
        return DEFAULT_IMAGE.withTag(DEFAULT_TAG);
    }

    public KubeMQContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public KubeMQContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE);

        addExposedPorts(GRPC_PORT, REST_PORT, API_PORT);
        waitingFor(Wait.forHttp("/health")
                .forPort(API_PORT)
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofSeconds(60)));
    }

    /**
     * Returns the gRPC address in {@code host:port} format, using the mapped port.
     */
    public String getGrpcAddress() {
        return getHost() + ":" + getMappedPort(GRPC_PORT);
    }

    /**
     * Returns the mapped gRPC port on the host.
     */
    public int getGrpcPort() {
        return getMappedPort(GRPC_PORT);
    }

    /**
     * Returns the REST API base URL.
     */
    public String getRestUrl() {
        return "http://" + getHost() + ":" + getMappedPort(REST_PORT);
    }

    /**
     * Returns the dashboard/API base URL.
     */
    public String getApiUrl() {
        return "http://" + getHost() + ":" + getMappedPort(API_PORT);
    }
}
