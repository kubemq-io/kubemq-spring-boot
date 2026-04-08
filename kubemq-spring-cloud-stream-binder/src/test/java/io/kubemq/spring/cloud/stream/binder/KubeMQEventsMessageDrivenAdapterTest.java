package io.kubemq.spring.cloud.stream.binder;

import io.kubemq.sdk.pubsub.EventsSubscription;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.spring.cloud.stream.binder.adapter.KubeMQEventsMessageDrivenAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.SubscribableChannel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KubeMQEventsMessageDrivenAdapterTest {

    @Mock
    private PubSubClient pubSubClient;

    private KubeMQHeaderMapper headerMapper;
    private KubeMQEventsMessageDrivenAdapter adapter;

    @BeforeEach
    void setUp() {
        headerMapper = new KubeMQHeaderMapper();
        adapter = new KubeMQEventsMessageDrivenAdapter(
                pubSubClient, "test-channel", "test-group", headerMapper);
        SubscribableChannel outputChannel = mock(SubscribableChannel.class);
        adapter.setOutputChannel(outputChannel);
    }

    @Test
    void start_subscribesToEventsChannel() {
        adapter.start();

        verify(pubSubClient).subscribeToEvents(any(EventsSubscription.class));

        adapter.stop();
    }

    @Test
    void stop_afterStart_cancelsSubscription() {
        adapter.start();
        adapter.stop();

        // After stop+start, a second subscription call should be made
        adapter.start();
        verify(pubSubClient, times(2)).subscribeToEvents(any(EventsSubscription.class));
        adapter.stop();
    }
}
