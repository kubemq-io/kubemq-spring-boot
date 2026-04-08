package io.kubemq.spring.boot.test;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import kubemq.Kubemq;
import kubemq.kubemqGrpc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MockKubeMQServiceTest {

    private MockKubeMQServer server;
    private ManagedChannel channel;
    private kubemqGrpc.kubemqBlockingStub stub;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockKubeMQServer();
        server.start();
        channel = server.getChannel();
        stub = kubemqGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void sendEvent_capturesReceivedEvent() {
        Kubemq.Event event = Kubemq.Event.newBuilder()
                .setChannel("events.test")
                .setEventID("evt-1")
                .setBody(ByteString.copyFromUtf8("hello"))
                .build();

        Kubemq.Result result = stub.sendEvent(event);

        assertThat(result.getSent()).isTrue();
        assertThat(result.getEventID()).isEqualTo("evt-1");

        List<Kubemq.Event> captured = server.getMockService().getReceivedEvents();
        assertThat(captured).hasSize(1);
        assertThat(captured.get(0).getChannel()).isEqualTo("events.test");
        assertThat(captured.get(0).getBody().toStringUtf8()).isEqualTo("hello");
    }

    @Test
    void sendRequest_capturesReceivedRequest() {
        Kubemq.Request request = Kubemq.Request.newBuilder()
                .setChannel("commands.test")
                .setRequestID("req-1")
                .setRequestTypeData(Kubemq.Request.RequestType.Command)
                .setBody(ByteString.copyFromUtf8("do-something"))
                .build();

        Kubemq.Response response = stub.sendRequest(request);

        assertThat(response.getExecuted()).isTrue();
        assertThat(response.getRequestID()).isEqualTo("req-1");

        List<Kubemq.Request> captured = server.getMockService().getReceivedRequests();
        assertThat(captured).hasSize(1);
        assertThat(captured.get(0).getChannel()).isEqualTo("commands.test");
    }

    @Test
    void emitEvent_sendsToActiveSubscriptionStreams() {
        // Subscribe first to create an active stream
        Kubemq.Subscribe subscribeRequest = Kubemq.Subscribe.newBuilder()
                .setChannel("events.test")
                .setSubscribeTypeData(Kubemq.Subscribe.SubscribeType.Events)
                .build();

        // Use async stub for streaming
        kubemqGrpc.kubemqStub asyncStub = kubemqGrpc.newStub(channel);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicReference<Kubemq.EventReceive> receivedRef =
                new java.util.concurrent.atomic.AtomicReference<>();

        asyncStub.subscribeToEvents(subscribeRequest, new io.grpc.stub.StreamObserver<>() {
            @Override
            public void onNext(Kubemq.EventReceive value) {
                receivedRef.set(value);
                latch.countDown();
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {}
        });

        // Wait a bit for stream registration
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        Kubemq.EventReceive emittedEvent = Kubemq.EventReceive.newBuilder()
                .setChannel("events.test")
                .setEventID("emitted-1")
                .setBody(ByteString.copyFromUtf8("emitted-body"))
                .build();

        server.getMockService().emitEvent(emittedEvent);

        try {
            latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}

        assertThat(receivedRef.get()).isNotNull();
        assertThat(receivedRef.get().getEventID()).isEqualTo("emitted-1");
    }

    @Test
    void reset_clearsAllCapturedData() {
        Kubemq.Event event = Kubemq.Event.newBuilder()
                .setChannel("events.test")
                .setEventID("evt-1")
                .build();
        stub.sendEvent(event);

        assertThat(server.getMockService().getReceivedEvents()).hasSize(1);

        server.getMockService().reset();

        assertThat(server.getMockService().getReceivedEvents()).isEmpty();
        assertThat(server.getMockService().getReceivedRequests()).isEmpty();
        assertThat(server.getMockService().getReceivedQueueMessages()).isEmpty();
    }

    @Test
    void ping_returnsConfiguredResponse() {
        Kubemq.PingResult ping = stub.ping(Kubemq.Empty.getDefaultInstance());

        assertThat(ping.getHost()).isEqualTo("mock-kubemq");
        assertThat(ping.getVersion()).isEqualTo("mock-1.0.0");
    }

    @Test
    void subscription_stream_stays_open() throws InterruptedException {
        Kubemq.Subscribe subscribeRequest = Kubemq.Subscribe.newBuilder()
                .setChannel("events.stream-test")
                .setSubscribeTypeData(Kubemq.Subscribe.SubscribeType.Events)
                .build();

        kubemqGrpc.kubemqStub asyncStub = kubemqGrpc.newStub(channel);
        java.util.concurrent.CountDownLatch firstLatch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch secondLatch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CopyOnWriteArrayList<Kubemq.EventReceive> received =
                new java.util.concurrent.CopyOnWriteArrayList<>();
        java.util.concurrent.atomic.AtomicBoolean completed = new java.util.concurrent.atomic.AtomicBoolean(false);

        asyncStub.subscribeToEvents(subscribeRequest, new io.grpc.stub.StreamObserver<>() {
            @Override
            public void onNext(Kubemq.EventReceive value) {
                received.add(value);
                if (received.size() == 1) firstLatch.countDown();
                if (received.size() == 2) secondLatch.countDown();
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });

        // Wait for stream registration
        Thread.sleep(100);

        // Emit first event
        server.getMockService().emitEvent(Kubemq.EventReceive.newBuilder()
                .setChannel("events.stream-test")
                .setEventID("evt-first")
                .setBody(ByteString.copyFromUtf8("first"))
                .build());

        firstLatch.await(5, java.util.concurrent.TimeUnit.SECONDS);

        // Stream should still be open (not completed)
        assertThat(completed.get()).isFalse();

        // Emit second event — verifies stream stays open after receiving the first
        server.getMockService().emitEvent(Kubemq.EventReceive.newBuilder()
                .setChannel("events.stream-test")
                .setEventID("evt-second")
                .setBody(ByteString.copyFromUtf8("second"))
                .build());

        secondLatch.await(5, java.util.concurrent.TimeUnit.SECONDS);

        assertThat(received).hasSize(2);
        assertThat(received.get(0).getEventID()).isEqualTo("evt-first");
        assertThat(received.get(1).getEventID()).isEqualTo("evt-second");
        assertThat(completed.get()).isFalse();
    }

    @Test
    void reset_restores_default_responses() {
        // Configure custom responses
        Kubemq.Response customCommandResponse = Kubemq.Response.newBuilder()
                .setRequestID("custom-id")
                .setExecuted(false)
                .setError("custom error")
                .build();
        server.getMockService().setNextCommandResponse(customCommandResponse);

        Kubemq.PingResult customPing = Kubemq.PingResult.newBuilder()
                .setHost("custom-host")
                .setVersion("custom-version")
                .build();
        server.getMockService().setPingResponse(customPing);

        server.getMockService().setErrorOnNextCall(
                new io.grpc.StatusRuntimeException(io.grpc.Status.UNAVAILABLE));

        // Reset should restore defaults
        server.getMockService().reset();

        // Ping should return default mock values (errorOnNextCall was cleared by reset)
        Kubemq.PingResult ping = stub.ping(Kubemq.Empty.getDefaultInstance());
        assertThat(ping.getHost()).isEqualTo("mock-kubemq");
        assertThat(ping.getVersion()).isEqualTo("mock-1.0.0");

        // Command response should return default executed=true
        Kubemq.Request request = Kubemq.Request.newBuilder()
                .setChannel("commands.test")
                .setRequestID("after-reset")
                .setRequestTypeData(Kubemq.Request.RequestType.Command)
                .build();

        Kubemq.Response response = stub.sendRequest(request);
        assertThat(response.getExecuted()).isTrue();
        assertThat(response.getRequestID()).isEqualTo("after-reset");
    }

    @Test
    void reset_closes_active_streams() throws InterruptedException {
        Kubemq.Subscribe subscribeRequest = Kubemq.Subscribe.newBuilder()
                .setChannel("events.reset-test")
                .setSubscribeTypeData(Kubemq.Subscribe.SubscribeType.Events)
                .build();

        kubemqGrpc.kubemqStub asyncStub = kubemqGrpc.newStub(channel);
        java.util.concurrent.CountDownLatch completedLatch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicBoolean completed = new java.util.concurrent.atomic.AtomicBoolean(false);

        asyncStub.subscribeToEvents(subscribeRequest, new io.grpc.stub.StreamObserver<>() {
            @Override
            public void onNext(Kubemq.EventReceive value) {}

            @Override
            public void onError(Throwable t) {
                completedLatch.countDown();
            }

            @Override
            public void onCompleted() {
                completed.set(true);
                completedLatch.countDown();
            }
        });

        // Wait for stream registration
        Thread.sleep(100);

        // Reset should close all active streams
        server.getMockService().reset();

        completedLatch.await(5, java.util.concurrent.TimeUnit.SECONDS);

        // Stream should have been completed by reset (which calls completeAllStreams)
        assertThat(completed.get()).isTrue();
    }
}
