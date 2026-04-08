package io.kubemq.spring.boot.test;

import io.grpc.stub.StreamObserver;
import kubemq.Kubemq;
import kubemq.kubemqGrpc;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * In-process gRPC service implementing {@link kubemqGrpc.kubemqImplBase} for
 * unit testing KubeMQ Spring Boot components without a real broker.
 *
 * <p>Captures all received messages for assertion and supports configurable
 * responses and error injection.
 *
 * <p>Usage:
 * <pre>{@code
 * MockKubeMQService service = new MockKubeMQService();
 * service.setNextCommandResponse(myResponse);
 * service.setErrorOnNextCall(new StatusRuntimeException(Status.UNAVAILABLE));
 * // after test
 * List<Kubemq.Event> events = service.getReceivedEvents();
 * }</pre>
 */
public class MockKubeMQService extends kubemqGrpc.kubemqImplBase {

    private final CopyOnWriteArrayList<Kubemq.Event> receivedEvents = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Kubemq.Request> receivedRequests = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Kubemq.Response> receivedResponses = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Kubemq.QueueMessage> receivedQueueMessages = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Kubemq.Subscribe> receivedSubscriptions = new CopyOnWriteArrayList<>();

    private final AtomicReference<Kubemq.PingResult> pingResponse =
            new AtomicReference<>(defaultPingResult());

    private final AtomicReference<Kubemq.Response> nextCommandResponse =
            new AtomicReference<>(defaultResponse());

    private final AtomicReference<Kubemq.Response> nextQueryResponse =
            new AtomicReference<>(defaultResponse());

    private final CopyOnWriteArrayList<StreamObserver<Kubemq.EventReceive>> activeEventStreams =
            new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<StreamObserver<Kubemq.Request>> activeRequestStreams =
            new CopyOnWriteArrayList<>();

    private volatile RuntimeException errorOnNextCall;
    private volatile List<Kubemq.EventReceive> eventsToEmit = List.of();
    private volatile List<Kubemq.Request> requestsToEmit = List.of();

    // --- Accessors for test assertions ---

    public List<Kubemq.Event> getReceivedEvents() {
        return List.copyOf(receivedEvents);
    }

    public List<Kubemq.Request> getReceivedRequests() {
        return List.copyOf(receivedRequests);
    }

    public List<Kubemq.Response> getReceivedResponses() {
        return List.copyOf(receivedResponses);
    }

    public List<Kubemq.QueueMessage> getReceivedQueueMessages() {
        return List.copyOf(receivedQueueMessages);
    }

    public List<Kubemq.Subscribe> getReceivedSubscriptions() {
        return List.copyOf(receivedSubscriptions);
    }

    // --- Configuration ---

    public void setNextCommandResponse(Kubemq.Response response) {
        nextCommandResponse.set(response);
    }

    public void setNextQueryResponse(Kubemq.Response response) {
        nextQueryResponse.set(response);
    }

    public void setPingResponse(Kubemq.PingResult response) {
        pingResponse.set(response);
    }

    public void setErrorOnNextCall(RuntimeException error) {
        this.errorOnNextCall = error;
    }

    public void setEventsToEmit(List<Kubemq.EventReceive> events) {
        this.eventsToEmit = List.copyOf(events);
    }

    public void setRequestsToEmit(List<Kubemq.Request> requests) {
        this.requestsToEmit = List.copyOf(requests);
    }

    /**
     * Emits an event to all active event subscription streams.
     * Streams that have been cancelled or errored are automatically removed.
     */
    public void emitEvent(Kubemq.EventReceive event) {
        for (StreamObserver<Kubemq.EventReceive> stream : activeEventStreams) {
            try {
                stream.onNext(event);
            } catch (Exception e) {
                activeEventStreams.remove(stream);
            }
        }
    }

    /**
     * Emits a request to all active request subscription streams.
     * Streams that have been cancelled or errored are automatically removed.
     */
    public void emitRequest(Kubemq.Request request) {
        for (StreamObserver<Kubemq.Request> stream : activeRequestStreams) {
            try {
                stream.onNext(request);
            } catch (Exception e) {
                activeRequestStreams.remove(stream);
            }
        }
    }

    /**
     * Completes all active subscription streams and clears the stream lists.
     */
    public void completeAllStreams() {
        for (StreamObserver<?> s : activeEventStreams) {
            try { s.onCompleted(); } catch (Exception ignored) {}
        }
        activeEventStreams.clear();
        for (StreamObserver<?> s : activeRequestStreams) {
            try { s.onCompleted(); } catch (Exception ignored) {}
        }
        activeRequestStreams.clear();
    }

    public void reset() {
        receivedEvents.clear();
        receivedRequests.clear();
        receivedResponses.clear();
        receivedQueueMessages.clear();
        receivedSubscriptions.clear();
        errorOnNextCall = null;
        eventsToEmit = List.of();
        requestsToEmit = List.of();
        pingResponse.set(defaultPingResult());
        nextCommandResponse.set(defaultResponse());
        nextQueryResponse.set(defaultResponse());
        completeAllStreams();
    }

    // --- gRPC method implementations ---

    @Override
    public void ping(Kubemq.Empty request, StreamObserver<Kubemq.PingResult> responseObserver) {
        if (maybeError(responseObserver)) return;
        responseObserver.onNext(pingResponse.get());
        responseObserver.onCompleted();
    }

    @Override
    public void sendEvent(Kubemq.Event request, StreamObserver<Kubemq.Result> responseObserver) {
        if (maybeError(responseObserver)) return;
        receivedEvents.add(request);
        Kubemq.Result result = Kubemq.Result.newBuilder()
                .setEventID(request.getEventID())
                .setSent(true)
                .build();
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Kubemq.Event> sendEventsStream(
            StreamObserver<Kubemq.Result> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(Kubemq.Event event) {
                receivedEvents.add(event);
                Kubemq.Result result = Kubemq.Result.newBuilder()
                        .setEventID(event.getEventID())
                        .setSent(true)
                        .build();
                responseObserver.onNext(result);
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void subscribeToEvents(Kubemq.Subscribe request,
                                  StreamObserver<Kubemq.EventReceive> responseObserver) {
        receivedSubscriptions.add(request);
        if (maybeError(responseObserver)) return;
        for (Kubemq.EventReceive event : eventsToEmit) {
            responseObserver.onNext(event);
        }
        activeEventStreams.add(responseObserver); // keep stream open
    }

    @Override
    public void subscribeToRequests(Kubemq.Subscribe request,
                                    StreamObserver<Kubemq.Request> responseObserver) {
        receivedSubscriptions.add(request);
        if (maybeError(responseObserver)) return;
        for (Kubemq.Request req : requestsToEmit) {
            responseObserver.onNext(req);
        }
        activeRequestStreams.add(responseObserver); // keep stream open
    }

    @Override
    public void sendRequest(Kubemq.Request request, StreamObserver<Kubemq.Response> responseObserver) {
        if (maybeError(responseObserver)) return;
        receivedRequests.add(request);

        Kubemq.Response.Builder builder;
        if (request.getRequestTypeData() == Kubemq.Request.RequestType.Command) {
            builder = nextCommandResponse.get().toBuilder();
        } else {
            builder = nextQueryResponse.get().toBuilder();
        }
        responseObserver.onNext(builder.setRequestID(request.getRequestID()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void sendResponse(Kubemq.Response request, StreamObserver<Kubemq.Empty> responseObserver) {
        if (maybeError(responseObserver)) return;
        receivedResponses.add(request);
        responseObserver.onNext(Kubemq.Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void sendQueueMessage(Kubemq.QueueMessage request,
                                 StreamObserver<Kubemq.SendQueueMessageResult> responseObserver) {
        if (maybeError(responseObserver)) return;
        receivedQueueMessages.add(request);
        Kubemq.SendQueueMessageResult result = Kubemq.SendQueueMessageResult.newBuilder()
                .setMessageID(request.getMessageID())
                .setSentAt(System.currentTimeMillis() * 1_000_000L)
                .setIsError(false)
                .build();
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

    @Override
    public void sendQueueMessagesBatch(Kubemq.QueueMessagesBatchRequest request,
                                       StreamObserver<Kubemq.QueueMessagesBatchResponse> responseObserver) {
        if (maybeError(responseObserver)) return;
        Kubemq.QueueMessagesBatchResponse.Builder builder =
                Kubemq.QueueMessagesBatchResponse.newBuilder()
                        .setBatchID(request.getBatchID());
        for (Kubemq.QueueMessage msg : request.getMessagesList()) {
            receivedQueueMessages.add(msg);
            builder.addResults(Kubemq.SendQueueMessageResult.newBuilder()
                    .setMessageID(msg.getMessageID())
                    .setSentAt(System.currentTimeMillis() * 1_000_000L)
                    .setIsError(false)
                    .build());
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void receiveQueueMessages(Kubemq.ReceiveQueueMessagesRequest request,
                                     StreamObserver<Kubemq.ReceiveQueueMessagesResponse> responseObserver) {
        if (maybeError(responseObserver)) return;
        Kubemq.ReceiveQueueMessagesResponse response =
                Kubemq.ReceiveQueueMessagesResponse.newBuilder()
                        .setRequestID(request.getRequestID())
                        .setIsError(false)
                        .setMessagesReceived(0)
                        .setMessagesExpired(0)
                        .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ackAllQueueMessages(Kubemq.AckAllQueueMessagesRequest request,
                                    StreamObserver<Kubemq.AckAllQueueMessagesResponse> responseObserver) {
        if (maybeError(responseObserver)) return;
        Kubemq.AckAllQueueMessagesResponse response =
                Kubemq.AckAllQueueMessagesResponse.newBuilder()
                        .setRequestID(request.getRequestID())
                        .setAffectedMessages(0)
                        .setIsError(false)
                        .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Kubemq.QueuesUpstreamRequest> queuesUpstream(
            StreamObserver<Kubemq.QueuesUpstreamResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(Kubemq.QueuesUpstreamRequest request) {
                Kubemq.QueuesUpstreamResponse.Builder builder =
                        Kubemq.QueuesUpstreamResponse.newBuilder()
                                .setRefRequestID(request.getRequestID())
                                .setIsError(false);
                for (Kubemq.QueueMessage msg : request.getMessagesList()) {
                    receivedQueueMessages.add(msg);
                    builder.addResults(Kubemq.SendQueueMessageResult.newBuilder()
                            .setMessageID(msg.getMessageID())
                            .setSentAt(System.currentTimeMillis() * 1_000_000L)
                            .setIsError(false)
                            .build());
                }
                responseObserver.onNext(builder.build());
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<Kubemq.QueuesDownstreamRequest> queuesDownstream(
            StreamObserver<Kubemq.QueuesDownstreamResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(Kubemq.QueuesDownstreamRequest request) {
                Kubemq.QueuesDownstreamResponse response =
                        Kubemq.QueuesDownstreamResponse.newBuilder()
                                .setRefRequestId(request.getRequestID())
                                .setTransactionId("tx-" + request.getRequestID())
                                .setIsError(false)
                                .setTransactionComplete(false)
                                .build();
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    private static Kubemq.PingResult defaultPingResult() {
        return Kubemq.PingResult.newBuilder()
                .setHost("mock-kubemq")
                .setVersion("mock-1.0.0")
                .setServerStartTime(System.currentTimeMillis())
                .setServerUpTimeSeconds(0)
                .build();
    }

    private static Kubemq.Response defaultResponse() {
        return Kubemq.Response.newBuilder()
                .setRequestID("mock-request-id")
                .setExecuted(true)
                .setTimestamp(System.currentTimeMillis() * 1_000_000L)
                .build();
    }

    private boolean maybeError(StreamObserver<?> observer) {
        RuntimeException error = errorOnNextCall;
        if (error != null) {
            errorOnNextCall = null;
            observer.onError(error);
            return true;
        }
        return false;
    }
}
