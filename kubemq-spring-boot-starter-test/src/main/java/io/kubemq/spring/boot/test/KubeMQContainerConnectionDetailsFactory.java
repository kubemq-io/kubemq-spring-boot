package io.kubemq.spring.boot.test;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;
import org.springframework.boot.autoconfigure.service.connection.ConnectionDetailsFactory;

/**
 * {@link ConnectionDetailsFactory} that produces {@link KubeMQConnectionDetails}
 * from a running {@link KubeMQContainer}.
 *
 * <p>Spring Boot's {@code @ServiceConnection} annotation uses this factory to
 * automatically wire container connection details into the application context
 * during integration tests.
 */
public class KubeMQContainerConnectionDetailsFactory
        implements ConnectionDetailsFactory<KubeMQContainer, KubeMQConnectionDetails> {

    @Override
    public KubeMQConnectionDetails getConnectionDetails(KubeMQContainer container) {
        return new KubeMQContainerConnectionDetails(container);
    }

    private static final class KubeMQContainerConnectionDetails implements KubeMQConnectionDetails {

        private final KubeMQContainer container;

        KubeMQContainerConnectionDetails(KubeMQContainer container) {
            this.container = container;
        }

        @Override
        public String getAddress() {
            return container.getGrpcAddress();
        }

        public String getOriginDescription() {
            return "KubeMQ TestContainer " + container.getDockerImageName();
        }
    }
}
