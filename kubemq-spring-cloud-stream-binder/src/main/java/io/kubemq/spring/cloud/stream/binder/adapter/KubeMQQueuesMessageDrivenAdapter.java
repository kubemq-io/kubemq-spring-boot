package io.kubemq.spring.cloud.stream.binder.adapter;

import io.kubemq.sdk.queues.QueueMessageReceived;
import io.kubemq.sdk.queues.QueuesClient;
import io.kubemq.sdk.queues.QueuesPollRequest;
import io.kubemq.sdk.queues.QueuesPollResponse;
import io.kubemq.spring.cloud.stream.binder.KubeMQHeaderMapper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Spring Cloud Stream consumer adapter for KubeMQ Queues (transactional receive).
 *
 * <p>Polls a KubeMQ queue channel via {@link QueuesClient#receiveQueueMessages} and forwards
 * each received message to the Spring Cloud Stream output channel. On successful processing
 * the message is acked; on failure it is nacked (rejected) so another consumer can retry.
 */
public class KubeMQQueuesMessageDrivenAdapter extends MessageProducerSupport {

    private static final Logger log = LoggerFactory.getLogger(KubeMQQueuesMessageDrivenAdapter.class);

    private final QueuesClient queuesClient;
    private final String channel;
    private final int pollMaxMessages;
    private final int pollWaitTimeoutInSeconds;
    private final int visibilitySeconds;
    private final boolean autoAckMessages;
    private final KubeMQHeaderMapper headerMapper;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile Thread pollingThread;
    private volatile long currentBackoffMs;
    private long initialBackoffMs = 1000L;
    private long maxBackoffMs = 30_000L;
    private double backoffMultiplier = 2.0;

    public KubeMQQueuesMessageDrivenAdapter(QueuesClient queuesClient, String channel,
                                             int pollMaxMessages, int pollWaitTimeoutInSeconds,
                                             int visibilitySeconds, boolean autoAckMessages,
                                             KubeMQHeaderMapper headerMapper) {
        this.queuesClient = queuesClient;
        this.channel = channel;
        this.pollMaxMessages = pollMaxMessages;
        this.pollWaitTimeoutInSeconds = pollWaitTimeoutInSeconds;
        this.visibilitySeconds = visibilitySeconds;
        this.autoAckMessages = autoAckMessages;
        this.headerMapper = headerMapper;
        this.currentBackoffMs = initialBackoffMs;
    }

    @Override
    protected void doStart() {
        log.info("Starting queue consumer on channel '{}' (maxMessages={}, waitTimeout={}s)",
                channel, pollMaxMessages, pollWaitTimeoutInSeconds);
        running.set(true);
        pollingThread = new Thread(this::pollLoop, "kubemq-queue-poller-" + channel);
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    @Override
    protected void doStop() {
        log.info("Stopping queue consumer on channel '{}'", channel);
        running.set(false);
        Thread t = this.pollingThread;
        if (t != null) {
            t.interrupt();
            try {
                t.join(5000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            this.pollingThread = null;
        }
    }

    private void pollLoop() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                QueuesPollRequest request = QueuesPollRequest.builder()
                        .channel(channel)
                        .pollMaxMessages(pollMaxMessages)
                        .pollWaitTimeoutInSeconds(pollWaitTimeoutInSeconds)
                        .visibilitySeconds(visibilitySeconds)
                        .autoAckMessages(autoAckMessages)
                        .build();

                QueuesPollResponse response = queuesClient.receiveQueueMessages(request);
                if (response.isError()) {
                    log.warn("Poll error on channel '{}': {}", channel, response.getError());
                    backoffSleep();
                    continue;
                }

                List<QueueMessageReceived> messages = response.getMessages();
                if (messages == null || messages.isEmpty()) {
                    resetBackoff();
                    continue;
                }

                for (QueueMessageReceived received : messages) {
                    processMessage(received);
                }
                resetBackoff();
            } catch (Exception ex) {
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }
                if (running.get()) {
                    log.error("Unexpected error polling queue channel '{}': {}",
                            channel, ex.getMessage(), ex);
                    backoffSleep();
                }
            }
        }
    }

    private void processMessage(QueueMessageReceived received) {
        try {
            Map<String, String> tags = received.getTags();
            MessageHeaders headers = headerMapper.toSpringHeaders(tags);
            Message<byte[]> message = MessageBuilder
                    .withPayload(received.getBody() != null ? received.getBody() : new byte[0])
                    .copyHeaders(headers)
                    .setHeader("kubemq_channel", received.getChannel())
                    .setHeader("kubemq_id", received.getId())
                    .setHeader("kubemq_metadata", received.getMetadata())
                    .setHeader("kubemq_sequence", received.getSequence())
                    .setHeader("kubemq_delivery_count", received.getReceiveCount())
                    .setHeader("kubemq_expiration_at", received.getExpiredAt())
                    .setHeader("kubemq_delayed_to", received.getDelayedTo())
                    .setHeader("kubemq_receiver_client_id", received.getReceiverClientId())
                    .build();
            sendMessage(message);
            if (!autoAckMessages) {
                received.ack();
            }
        } catch (Exception ex) {
            log.error("Error processing queue message from channel '{}': {}",
                    channel, ex.getMessage(), ex);
            if (!autoAckMessages) {
                try {
                    received.reject();
                } catch (Exception rejectEx) {
                    log.error("Failed to reject message on channel '{}': {}",
                            channel, rejectEx.getMessage(), rejectEx);
                }
            }
        }
    }

    private void backoffSleep() {
        long delay = currentBackoffMs;
        log.debug("Backing off {}ms before retrying poll on channel '{}'", delay, channel);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        currentBackoffMs = Math.min((long) (delay * backoffMultiplier), maxBackoffMs);
    }

    private void resetBackoff() {
        currentBackoffMs = initialBackoffMs;
    }
}
