package io.kubemq.spring.boot.autoconfigure.template;

import io.kubemq.sdk.cq.CommandMessage;
import io.kubemq.sdk.cq.CommandResponseMessage;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fluent builder for sending command messages via {@link KubeMQTemplate}.
 *
 * <p>Obtain an instance from {@link KubeMQTemplate#newCommand(Object)}.
 */
public class KubeMQCommandMessageBuilder {

    private final KubeMQTemplate template;
    private final Object data;
    private String channel;
    private String metadata;
    private final Map<String, String> tags = new HashMap<>();
    private Duration timeout;

    KubeMQCommandMessageBuilder(KubeMQTemplate template, Object data) {
        this.template = template;
        this.data = data;
    }

    public KubeMQCommandMessageBuilder toChannel(String channel) {
        this.channel = channel;
        return this;
    }

    public KubeMQCommandMessageBuilder withTag(String key, String value) {
        this.tags.put(key, value);
        return this;
    }

    public KubeMQCommandMessageBuilder withTags(Map<String, String> tags) {
        if (tags != null) {
            this.tags.putAll(tags);
        }
        return this;
    }

    public KubeMQCommandMessageBuilder withTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public KubeMQCommandMessageBuilder withMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    @SuppressWarnings("deprecation")
    public CommandResponseMessage send() {
        Map<String, String> mutableTags = new HashMap<>(tags);
        byte[] body = template.getMessageConverter() != null
                ? template.getMessageConverter().toBytes(data, mutableTags)
                : KubeMQTemplate.defaultSerialize(data);

        CommandMessage.CommandMessageBuilder builder = CommandMessage.builder()
                .channel(channel)
                .body(body)
                .tags(mutableTags)
                .timeoutInSeconds(KubeMQTemplate.durationToSeconds(timeout));
        if (metadata != null) {
            builder.metadata(metadata);
        }

        return template.getCqClient().sendCommandRequest(builder.build());
    }

    @SuppressWarnings("deprecation")
    public CompletableFuture<CommandResponseMessage> sendAsync() {
        Map<String, String> mutableTags = new HashMap<>(tags);
        byte[] body = template.getMessageConverter() != null
                ? template.getMessageConverter().toBytes(data, mutableTags)
                : KubeMQTemplate.defaultSerialize(data);

        CommandMessage.CommandMessageBuilder builder = CommandMessage.builder()
                .channel(channel)
                .body(body)
                .tags(mutableTags)
                .timeoutInSeconds(KubeMQTemplate.durationToSeconds(timeout));
        if (metadata != null) {
            builder.metadata(metadata);
        }

        return template.getCqClient().sendCommandRequestAsync(builder.build());
    }
}
