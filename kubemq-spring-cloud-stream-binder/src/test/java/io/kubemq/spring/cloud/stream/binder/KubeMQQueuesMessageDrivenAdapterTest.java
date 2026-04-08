package io.kubemq.spring.cloud.stream.binder;

import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.spring.cloud.stream.binder.adapter.KubeMQQueuesMessageDrivenAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.SubscribableChannel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class KubeMQQueuesMessageDrivenAdapterTest {

    @Mock
    private QueuesClient queuesClient;

    private KubeMQHeaderMapper headerMapper;

    @BeforeEach
    void setUp() {
        headerMapper = new KubeMQHeaderMapper();
    }

    @Test
    void adapter_constructsWithExpectedParameters() {
        KubeMQQueuesMessageDrivenAdapter adapter = new KubeMQQueuesMessageDrivenAdapter(
                queuesClient, "queue-channel", 10, 5, 30, true, headerMapper);
        assertThat(adapter).isNotNull();
    }

    @Test
    void startAndStop_startsAndStopsPollingThread() throws InterruptedException {
        KubeMQQueuesMessageDrivenAdapter adapter = new KubeMQQueuesMessageDrivenAdapter(
                queuesClient, "queue-channel", 1, 1, 30, true, headerMapper);
        SubscribableChannel outputChannel = mock(SubscribableChannel.class);
        adapter.setOutputChannel(outputChannel);

        adapter.start();
        // Give the polling thread a moment to start
        Thread.sleep(100);
        adapter.stop();

        assertThat(adapter.isRunning()).isFalse();
    }

    @Test
    void restartAfterStop_createsNewPollingThread() throws InterruptedException {
        KubeMQQueuesMessageDrivenAdapter adapter = new KubeMQQueuesMessageDrivenAdapter(
                queuesClient, "queue-channel", 1, 1, 30, true, headerMapper);
        SubscribableChannel outputChannel = mock(SubscribableChannel.class);
        adapter.setOutputChannel(outputChannel);

        adapter.start();
        Thread.sleep(50);
        adapter.stop();

        adapter.start();
        assertThat(adapter.isRunning()).isTrue();
        adapter.stop();
    }
}
