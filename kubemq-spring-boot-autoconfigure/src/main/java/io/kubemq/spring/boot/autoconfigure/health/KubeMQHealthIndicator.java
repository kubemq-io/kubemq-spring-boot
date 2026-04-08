package io.kubemq.spring.boot.autoconfigure.health;

import io.kubemq.sdk.common.ServerInfo;
import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * Health indicator that pings all three KubeMQ broker clients and reports connectivity status.
 *
 * <p>Reports {@code UP} with server details when all clients respond to a ping,
 * and {@code DOWN} with exception details on failure. Results are cached for
 * {@code cacheDuration} to avoid overwhelming the broker with health probes.
 */
public class KubeMQHealthIndicator extends AbstractHealthIndicator {

    private final PubSubClient pubSubClient;
    private final QueuesClient queuesClient;
    private final CQClient cqClient;
    private final Duration timeout;
    private final Duration cacheDuration;
    private volatile Health cachedHealth;
    private volatile long lastCheckTime;

    public KubeMQHealthIndicator(
            PubSubClient pubSubClient,
            QueuesClient queuesClient,
            CQClient cqClient,
            Duration timeout,
            Duration cacheDuration) {
        super("KubeMQ health check failed");
        this.pubSubClient = pubSubClient;
        this.queuesClient = queuesClient;
        this.cqClient = cqClient;
        this.timeout = timeout;
        this.cacheDuration = cacheDuration;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        long now = System.currentTimeMillis();
        // Non-atomic cache check is intentional -- occasional duplicate probes acceptable
        if (cachedHealth != null && (now - lastCheckTime) < cacheDuration.toMillis()) {
            builder.status(cachedHealth.getStatus()).withDetails(cachedHealth.getDetails());
            return;
        }
        // Use orTimeout() (Java 9+) on each future to ensure self-cancellation on timeout,
        // preventing task leaks when broker is unavailable
        long timeoutMs = timeout.toMillis();
        CompletableFuture<ServerInfo> pubsubPing = CompletableFuture
                .supplyAsync(() -> pubSubClient.ping())
                .orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
        CompletableFuture<ServerInfo> queuesPing = CompletableFuture
                .supplyAsync(() -> queuesClient.ping())
                .orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
        CompletableFuture<ServerInfo> cqPing = CompletableFuture
                .supplyAsync(() -> cqClient.ping())
                .orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
        try {
            CompletableFuture.allOf(pubsubPing, queuesPing, cqPing).join();
            ServerInfo info = pubsubPing.join();
            builder.up()
                    .withDetail("host", info.getHost())
                    .withDetail("version", info.getVersion());
        } catch (Exception e) {
            builder.down().withDetail("error", e.getClass().getSimpleName());
        }
        cachedHealth = builder.build();
        lastCheckTime = now;
    }
}
