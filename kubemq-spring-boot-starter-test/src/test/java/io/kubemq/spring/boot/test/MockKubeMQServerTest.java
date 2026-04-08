package io.kubemq.spring.boot.test;

import io.grpc.ManagedChannel;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MockKubeMQServerTest {

    @Test
    void startAndStop_serverStartsAndClosesCleanly() throws IOException {
        MockKubeMQServer server = new MockKubeMQServer();
        server.start();

        assertThat(server.getChannel()).isNotNull();
        assertThat(server.getMockService()).isNotNull();

        server.close();
    }

    @Test
    void getChannel_beforeStart_throwsIllegalStateException() {
        MockKubeMQServer server = new MockKubeMQServer();

        assertThatThrownBy(server::getChannel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not been started");
    }

    @Test
    void getChannel_returnsUsableChannel() throws IOException {
        MockKubeMQServer server = new MockKubeMQServer();
        server.start();

        ManagedChannel channel = server.getChannel();
        assertThat(channel).isNotNull();
        assertThat(channel.isShutdown()).isFalse();

        server.close();
    }

    @Test
    void getMockService_returnsSameInstance() throws IOException {
        MockKubeMQServer server = new MockKubeMQServer();
        server.start();

        MockKubeMQService service1 = server.getMockService();
        MockKubeMQService service2 = server.getMockService();
        assertThat(service1).isSameAs(service2);

        server.close();
    }
}
