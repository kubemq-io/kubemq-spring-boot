package io.kubemq.spring.boot.autoconfigure.actuator;

import io.kubemq.sdk.client.KubeMQClient;
import io.kubemq.sdk.common.ServerInfo;
import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesClient;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

/**
 * Actuator endpoint exposing KubeMQ broker connection details at
 * {@code /actuator/kubemq}.
 *
 * <p>Probes all three clients (pubsub, queues, cq) and reports per-client status.
 */
@Endpoint(id = "kubemq")
public class KubeMQEndpoint {

    private static final Logger log = LoggerFactory.getLogger(KubeMQEndpoint.class);

    private final PubSubClient pubSubClient;
    private final QueuesClient queuesClient;
    private final CQClient cqClient;

    public KubeMQEndpoint(PubSubClient pubSubClient, QueuesClient queuesClient, CQClient cqClient) {
        this.pubSubClient = pubSubClient;
        this.queuesClient = queuesClient;
        this.cqClient = cqClient;
    }

    @ReadOperation
    public KubeMQInfo kubemqInfo() {
        Map<String, ClientStatus> clientStatuses = new LinkedHashMap<>();
        clientStatuses.put("pubsub", probeClient(pubSubClient));
        clientStatuses.put("queues", probeClient(queuesClient));
        clientStatuses.put("cq", probeClient(cqClient));
        boolean allConnected = clientStatuses.values().stream()
                .allMatch(s -> "connected".equals(s.status()));
        return new KubeMQInfo(allConnected ? "connected" : "degraded", clientStatuses);
    }

    private ClientStatus probeClient(KubeMQClient client) {
        try {
            ServerInfo info = client.ping();
            return new ClientStatus("connected", info.getHost(), info.getVersion(), null);
        } catch (Exception e) {
            log.warn("KubeMQ client probe failed: {}", e.getMessage());
            return new ClientStatus("disconnected", null, null, e.getClass().getSimpleName());
        }
    }

    /**
     * Per-client connection status.
     */
    public record ClientStatus(String status, String host, String version, String errorType) {}

    /**
     * JSON-serialisable response returned by the {@code /actuator/kubemq} endpoint.
     */
    public record KubeMQInfo(String status, Map<String, ClientStatus> clients) {}
}
