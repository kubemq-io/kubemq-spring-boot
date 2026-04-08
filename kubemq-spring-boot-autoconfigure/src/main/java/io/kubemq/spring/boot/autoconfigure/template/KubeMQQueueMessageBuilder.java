package io.kubemq.spring.boot.autoconfigure.template;

import io.kubemq.sdk.queues.QueueMessage;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fluent builder for sending queue messages via {@link KubeMQTemplate}.
 *
 * <p>Obtain an instance from {@link KubeMQTemplate#newQueueMessage(Object)}.
 */
public class KubeMQQueueMessageBuilder {

    private final KubeMQTemplate template;
    private final Object data;
    private String channel;
    private String metadata;
    private final Map<String, String> tags = new HashMap<>();
    private Duration expiration;
    private Duration delay;
    private String deadLetterChannel;
    private int maxReceiveCount;

    KubeMQQueueMessageBuilder(KubeMQTemplate template, Object data) {
        this.template = template;
        this.data = data;
    }

    public KubeMQQueueMessageBuilder toChannel(String channel) {
        this.channel = channel;
        return this;
    }

    public KubeMQQueueMessageBuilder withTag(String key, String value) {
        this.tags.put(key, value);
        return this;
    }

    public KubeMQQueueMessageBuilder withTags(Map<String, String> tags) {
        if (tags != null) {
            this.tags.putAll(tags);
        }
        return this;
    }

    public KubeMQQueueMessageBuilder withMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    public KubeMQQueueMessageBuilder withExpiration(Duration expiration) {
        this.expiration = expiration;
        return this;
    }

    public KubeMQQueueMessageBuilder withDelay(Duration delay) {
        this.delay = delay;
        return this;
    }

    public KubeMQQueueMessageBuilder withDeadLetterQueue(String deadLetterChannel, int maxReceiveCount) {
        this.deadLetterChannel = deadLetterChannel;
        this.maxReceiveCount = maxReceiveCount;
        return this;
    }

    public void send() {
        Map<String, String> mutableTags = new HashMap<>(tags);
        byte[] body = template.getMessageConverter() != null
                ? template.getMessageConverter().toBytes(data, mutableTags)
                : KubeMQTemplate.defaultSerialize(data);

        QueueMessage.QueueMessageBuilder builder = QueueMessage.builder()
                .channel(channel)
                .body(body)
                .tags(mutableTags);

        if (metadata != null) {
            builder.metadata(metadata);
        }
        if (expiration != null) {
            builder.expirationInSeconds(KubeMQTemplate.durationToSeconds(expiration));
        }
        if (delay != null) {
            builder.delayInSeconds(KubeMQTemplate.durationToSeconds(delay));
        }
        if (deadLetterChannel != null) {
            builder.deadLetterQueue(deadLetterChannel);
            builder.attemptsBeforeDeadLetterQueue(maxReceiveCount);
        }

        template.getQueuesClient().sendQueueMessage(builder.build());
    }

    public CompletableFuture<Void> sendAsync() {
        Map<String, String> mutableTags = new HashMap<>(tags);
        byte[] body = template.getMessageConverter() != null
                ? template.getMessageConverter().toBytes(data, mutableTags)
                : KubeMQTemplate.defaultSerialize(data);

        QueueMessage.QueueMessageBuilder builder = QueueMessage.builder()
                .channel(channel)
                .body(body)
                .tags(mutableTags);

        if (metadata != null) {
            builder.metadata(metadata);
        }
        if (expiration != null) {
            builder.expirationInSeconds(KubeMQTemplate.durationToSeconds(expiration));
        }
        if (delay != null) {
            builder.delayInSeconds(KubeMQTemplate.durationToSeconds(delay));
        }
        if (deadLetterChannel != null) {
            builder.deadLetterQueue(deadLetterChannel);
            builder.attemptsBeforeDeadLetterQueue(maxReceiveCount);
        }

        return template.getQueuesClient().sendQueuesMessageAsync(builder.build())
                .thenApply(result -> null);
    }
}
