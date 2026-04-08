package io.kubemq.spring.boot.autoconfigure.template;

import io.kubemq.sdk.pubsub.EventMessage;
import io.kubemq.sdk.pubsub.EventStoreMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fluent builder for sending event or event-store messages via {@link KubeMQTemplate}.
 *
 * <p>Obtain an instance from {@link KubeMQTemplate#newEvent(Object)}
 * or {@link KubeMQTemplate#newEventStore(Object)}.
 */
public class KubeMQEventMessageBuilder {

    private final KubeMQTemplate template;
    private final Object data;
    private final boolean eventStore;
    private String channel;
    private String metadata;
    private final Map<String, String> tags = new HashMap<>();

    KubeMQEventMessageBuilder(KubeMQTemplate template, Object data, boolean eventStore) {
        this.template = template;
        this.data = data;
        this.eventStore = eventStore;
    }

    public KubeMQEventMessageBuilder toChannel(String channel) {
        this.channel = channel;
        return this;
    }

    public KubeMQEventMessageBuilder withTag(String key, String value) {
        this.tags.put(key, value);
        return this;
    }

    public KubeMQEventMessageBuilder withTags(Map<String, String> tags) {
        if (tags != null) {
            this.tags.putAll(tags);
        }
        return this;
    }

    public KubeMQEventMessageBuilder withMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    public void send() {
        Map<String, String> mutableTags = new HashMap<>(tags);
        byte[] body = template.getMessageConverter() != null
                ? template.getMessageConverter().toBytes(data, mutableTags)
                : KubeMQTemplate.defaultSerialize(data);

        if (eventStore) {
            EventStoreMessage.EventStoreMessageBuilder builder = EventStoreMessage.builder()
                    .channel(channel).body(body).tags(mutableTags);
            if (metadata != null) { builder.metadata(metadata); }
            template.getPubSubClient().publishEventStore(builder.build());
        } else {
            EventMessage.EventMessageBuilder builder = EventMessage.builder()
                    .channel(channel).body(body).tags(mutableTags);
            if (metadata != null) { builder.metadata(metadata); }
            template.getPubSubClient().publishEvent(builder.build());
        }
    }

    public CompletableFuture<Void> sendAsync() {
        Map<String, String> mutableTags = new HashMap<>(tags);
        byte[] body = template.getMessageConverter() != null
                ? template.getMessageConverter().toBytes(data, mutableTags)
                : KubeMQTemplate.defaultSerialize(data);

        if (eventStore) {
            EventStoreMessage.EventStoreMessageBuilder builder = EventStoreMessage.builder()
                    .channel(channel).body(body).tags(mutableTags);
            if (metadata != null) { builder.metadata(metadata); }
            return template.getPubSubClient().sendEventsStoreMessageAsync(builder.build())
                    .thenApply(result -> null);
        } else {
            EventMessage.EventMessageBuilder builder = EventMessage.builder()
                    .channel(channel).body(body).tags(mutableTags);
            if (metadata != null) { builder.metadata(metadata); }
            return template.getPubSubClient().sendEventsMessageAsync(builder.build());
        }
    }
}
