package io.kubemq.spring.boot.examples.management;

import io.kubemq.sdk.cq.CQChannel;
import io.kubemq.sdk.cq.CQClient;
import io.kubemq.sdk.pubsub.PubSubChannel;
import io.kubemq.sdk.pubsub.PubSubClient;
import io.kubemq.sdk.queues.QueuesChannel;
import io.kubemq.sdk.queues.QueuesClient;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ListChannelsRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ListChannelsRunner.class);

    private final PubSubClient pubSubClient;
    private final CQClient cqClient;
    private final QueuesClient queuesClient;

    public ListChannelsRunner(PubSubClient pubSubClient, CQClient cqClient, QueuesClient queuesClient) {
        this.pubSubClient = pubSubClient;
        this.cqClient = cqClient;
        this.queuesClient = queuesClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<PubSubChannel> eventsChannels = pubSubClient.listEventsChannels("spring-mgmt");
            log.info("Events channels matching 'spring-mgmt': {}", eventsChannels.size());
            for (PubSubChannel ch : eventsChannels) {
                log.info("  Events channel: {}", ch.getName());
            }

            List<PubSubChannel> storeChannels = pubSubClient.listEventsStoreChannels("spring-mgmt");
            log.info("Events store channels matching 'spring-mgmt': {}", storeChannels.size());

            List<CQChannel> cmdChannels = cqClient.listCommandsChannels("spring-mgmt");
            log.info("Commands channels matching 'spring-mgmt': {}", cmdChannels.size());

            List<CQChannel> queryChannels = cqClient.listQueriesChannels("spring-mgmt");
            log.info("Queries channels matching 'spring-mgmt': {}", queryChannels.size());

            List<QueuesChannel> queueChannels = queuesClient.listQueuesChannels("spring-mgmt");
            log.info("Queue channels matching 'spring-mgmt': {}", queueChannels.size());

            log.info("List channels example completed.");
        } catch (Exception ex) {
            log.warn("Could not run example (is KubeMQ running?): {}", ex);
        }
    }
}

// Expected output:
// INFO  Events channels matching 'spring-mgmt': <count>
// INFO    Events channel: spring-mgmt.events-channel
// INFO  Events store channels matching 'spring-mgmt': <count>
// INFO  Commands channels matching 'spring-mgmt': <count>
// INFO  Queries channels matching 'spring-mgmt': <count>
// INFO  Queue channels matching 'spring-mgmt': <count>
// INFO  List channels example completed.
