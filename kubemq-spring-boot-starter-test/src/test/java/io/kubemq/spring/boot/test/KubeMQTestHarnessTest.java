package io.kubemq.spring.boot.test;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import kubemq.Kubemq;
import kubemq.kubemqGrpc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KubeMQTestHarnessTest {

    private MockKubeMQServer server;
    private KubeMQTestHarness harness;
    private kubemqGrpc.kubemqBlockingStub stub;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockKubeMQServer();
        server.start();
        harness = new KubeMQTestHarness(server.getMockService());
        stub = kubemqGrpc.newBlockingStub(server.getChannel());
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void expectEvent_matchesReceivedEvent() {
        Kubemq.Event event = Kubemq.Event.newBuilder()
                .setChannel("events.orders")
                .setEventID("evt-1")
                .setBody(ByteString.copyFromUtf8("order-123"))
                .build();
        stub.sendEvent(event);

        // Should not throw
        harness.expectEvent()
                .onChannel("events.orders")
                .withBodyContaining("order-123")
                .receivedWithin(Duration.ofSeconds(5));
    }

    @Test
    void expectNoEvents_succeedsWhenNoEventsReceived() {
        // No events sent; this should pass without error
        harness.expectNoEvents(Duration.ofMillis(500));
    }

    @Test
    void expectNoEvents_failsWhenEventExists() {
        Kubemq.Event event = Kubemq.Event.newBuilder()
                .setChannel("events.test")
                .setEventID("evt-1")
                .build();
        stub.sendEvent(event);

        assertThatThrownBy(() -> harness.expectNoEvents(Duration.ofMillis(500)))
                .isInstanceOf(org.awaitility.core.ConditionTimeoutException.class);
    }

    @Test
    void capturedDomainTypes_wrapProtobufCorrectly() {
        Kubemq.Event protoEvent = Kubemq.Event.newBuilder()
                .setChannel("test-ch")
                .setEventID("id-1")
                .setBody(ByteString.copyFromUtf8("body"))
                .setMetadata("meta")
                .putTags("key", "value")
                .build();

        CapturedEvent captured = CapturedEvent.from(protoEvent);
        assertThat(captured.channel()).isEqualTo("test-ch");
        assertThat(captured.id()).isEqualTo("id-1");
        assertThat(captured.body()).isEqualTo("body".getBytes());
        assertThat(captured.metadata()).isEqualTo("meta");
        assertThat(captured.tags()).containsEntry("key", "value");

        Kubemq.Request protoRequest = Kubemq.Request.newBuilder()
                .setChannel("commands.test")
                .setRequestID("req-1")
                .setRequestTypeData(Kubemq.Request.RequestType.Command)
                .setBody(ByteString.copyFromUtf8("cmd"))
                .setMetadata("cmd-meta")
                .build();

        CapturedRequest capturedReq = CapturedRequest.from(protoRequest);
        assertThat(capturedReq.channel()).isEqualTo("commands.test");
        assertThat(capturedReq.requestId()).isEqualTo("req-1");
        assertThat(capturedReq.requestType()).isEqualTo("Command");

        Kubemq.QueueMessage protoQueue = Kubemq.QueueMessage.newBuilder()
                .setChannel("queues.test")
                .setMessageID("msg-1")
                .setBody(ByteString.copyFromUtf8("queue-body"))
                .setMetadata("q-meta")
                .build();

        CapturedQueueMessage capturedQ = CapturedQueueMessage.from(protoQueue);
        assertThat(capturedQ.channel()).isEqualTo("queues.test");
        assertThat(capturedQ.messageId()).isEqualTo("msg-1");
        assertThat(capturedQ.body()).isEqualTo("queue-body".getBytes());
    }
}
