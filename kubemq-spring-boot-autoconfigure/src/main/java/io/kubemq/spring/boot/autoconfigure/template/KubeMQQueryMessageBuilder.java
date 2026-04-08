package io.kubemq.spring.boot.autoconfigure.template;

import io.kubemq.sdk.cq.QueryMessage;
import io.kubemq.sdk.cq.QueryResponseMessage;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fluent builder for sending query messages via {@link KubeMQTemplate}.
 *
 * <p>Obtain an instance from {@link KubeMQTemplate#newQuery(Object)}.
 */
public class KubeMQQueryMessageBuilder {

    private final KubeMQTemplate template;
    private final Object data;
    private String channel;
    private String metadata;
    private final Map<String, String> tags = new HashMap<>();
    private Duration timeout;
    private String cacheKey;
    private Duration cacheTTL;

    KubeMQQueryMessageBuilder(KubeMQTemplate template, Object data) {
        this.template = template;
        this.data = data;
    }

    public KubeMQQueryMessageBuilder toChannel(String channel) {
        this.channel = channel;
        return this;
    }

    public KubeMQQueryMessageBuilder withTag(String key, String value) {
        this.tags.put(key, value);
        return this;
    }

    public KubeMQQueryMessageBuilder withTags(Map<String, String> tags) {
        if (tags != null) {
            this.tags.putAll(tags);
        }
        return this;
    }

    public KubeMQQueryMessageBuilder withTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public KubeMQQueryMessageBuilder withCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
        return this;
    }

    public KubeMQQueryMessageBuilder withCacheTTL(Duration cacheTTL) {
        this.cacheTTL = cacheTTL;
        return this;
    }

    public KubeMQQueryMessageBuilder withMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    @SuppressWarnings("deprecation")
    public QueryResponseMessage send() {
        Map<String, String> mutableTags = new HashMap<>(tags);
        byte[] body = template.getMessageConverter() != null
                ? template.getMessageConverter().toBytes(data, mutableTags)
                : KubeMQTemplate.defaultSerialize(data);

        QueryMessage.QueryMessageBuilder builder = QueryMessage.builder()
                .channel(channel)
                .body(body)
                .tags(mutableTags)
                .timeoutInSeconds(KubeMQTemplate.durationToSeconds(timeout));

        if (metadata != null) {
            builder.metadata(metadata);
        }
        if (cacheKey != null) {
            builder.cacheKey(cacheKey);
        }
        if (cacheTTL != null) {
            builder.cacheTtlInSeconds(KubeMQTemplate.durationToSeconds(cacheTTL));
        }

        return template.getCqClient().sendQueryRequest(builder.build());
    }

    @SuppressWarnings("deprecation")
    public CompletableFuture<QueryResponseMessage> sendAsync() {
        Map<String, String> mutableTags = new HashMap<>(tags);
        byte[] body = template.getMessageConverter() != null
                ? template.getMessageConverter().toBytes(data, mutableTags)
                : KubeMQTemplate.defaultSerialize(data);

        QueryMessage.QueryMessageBuilder builder = QueryMessage.builder()
                .channel(channel)
                .body(body)
                .tags(mutableTags)
                .timeoutInSeconds(KubeMQTemplate.durationToSeconds(timeout));

        if (metadata != null) {
            builder.metadata(metadata);
        }
        if (cacheKey != null) {
            builder.cacheKey(cacheKey);
        }
        if (cacheTTL != null) {
            builder.cacheTtlInSeconds(KubeMQTemplate.durationToSeconds(cacheTTL));
        }

        return template.getCqClient().sendQueryRequestAsync(builder.build());
    }
}
