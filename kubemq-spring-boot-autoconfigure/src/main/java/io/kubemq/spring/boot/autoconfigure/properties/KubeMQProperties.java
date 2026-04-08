package io.kubemq.spring.boot.autoconfigure.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * Configuration properties for KubeMQ Spring Boot starter.
 *
 * <p>Maps to the {@code kubemq.*} prefix in {@code application.yml}.
 */
@ConfigurationProperties(prefix = "kubemq")
public class KubeMQProperties {

    /**
     * Master switch for all KubeMQ auto-configuration.
     */
    private boolean enabled = true;

    /**
     * KubeMQ server gRPC address in {@code host:port} format.
     */
    private String address = "localhost:50000";

    /**
     * Client identifier sent with every request. Defaults to empty (SDK generates a UUID).
     */
    private String clientId = "";

    /**
     * JWT/OIDC authentication token.
     */
    private String authToken = "";

    private final Tls tls = new Tls();

    private final Connection connection = new Connection();

    private final Listener listener = new Listener();

    private final Template template = new Template();

    private final Health health = new Health();

    private final Metrics metrics = new Metrics();

    private final Kotlin kotlin = new Kotlin();

    // --- top-level getters/setters ---

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Tls getTls() {
        return tls;
    }

    public Connection getConnection() {
        return connection;
    }

    public Listener getListener() {
        return listener;
    }

    public Template getTemplate() {
        return template;
    }

    public Health getHealth() {
        return health;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public Kotlin getKotlin() {
        return kotlin;
    }

    // ============================
    // Nested configuration classes
    // ============================

    /**
     * TLS/mTLS configuration for the gRPC connection.
     */
    public static class Tls {

        private boolean enabled = false;

        private String certFile = "";

        private String keyFile = "";

        private String caCertFile = "";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getCertFile() {
            return certFile;
        }

        public void setCertFile(String certFile) {
            this.certFile = certFile;
        }

        public String getKeyFile() {
            return keyFile;
        }

        public void setKeyFile(String keyFile) {
            this.keyFile = keyFile;
        }

        public String getCaCertFile() {
            return caCertFile;
        }

        public void setCaCertFile(String caCertFile) {
            this.caCertFile = caCertFile;
        }
    }

    /**
     * gRPC connection tuning.
     */
    public static class Connection {

        private Duration timeout = Duration.ofSeconds(30);

        private DataSize maxReceiveSize = DataSize.ofMegabytes(100);

        private final KeepAlive keepAlive = new KeepAlive();

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public DataSize getMaxReceiveSize() {
            return maxReceiveSize;
        }

        public void setMaxReceiveSize(DataSize maxReceiveSize) {
            this.maxReceiveSize = maxReceiveSize;
        }

        public KeepAlive getKeepAlive() {
            return keepAlive;
        }
    }

    /**
     * gRPC keep-alive settings.
     */
    public static class KeepAlive {

        private Duration time = Duration.ofSeconds(30);

        private Duration timeout = Duration.ofSeconds(10);

        public Duration getTime() {
            return time;
        }

        public void setTime(Duration time) {
            this.time = time;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }
    }

    /**
     * Top-level listener defaults shared across all pattern-specific listeners.
     */
    public static class Listener {

        private int concurrency = 1;

        private boolean autoStartup = true;

        private Duration shutdownTimeout = Duration.ofSeconds(30);

        private final EventsListener events = new EventsListener();

        private final QueuesListener queues = new QueuesListener();

        private final CommandsListener commands = new CommandsListener();

        private final QueriesListener queries = new QueriesListener();

        public int getConcurrency() {
            return concurrency;
        }

        public void setConcurrency(int concurrency) {
            this.concurrency = concurrency;
        }

        public boolean isAutoStartup() {
            return autoStartup;
        }

        public void setAutoStartup(boolean autoStartup) {
            this.autoStartup = autoStartup;
        }

        public Duration getShutdownTimeout() {
            return shutdownTimeout;
        }

        public void setShutdownTimeout(Duration shutdownTimeout) {
            this.shutdownTimeout = shutdownTimeout;
        }

        public EventsListener getEvents() {
            return events;
        }

        public QueuesListener getQueues() {
            return queues;
        }

        public CommandsListener getCommands() {
            return commands;
        }

        public QueriesListener getQueries() {
            return queries;
        }
    }

    /**
     * Events-specific listener settings.
     *
     * <p>Note: Events are push-based subscriptions in KubeMQ and have no
     * poll-related settings. This class is a placeholder for future
     * events-specific configuration.
     */
    public static class EventsListener {
        // Push-based subscription -- no poll settings needed.
    }

    /**
     * Queues-specific listener settings.
     */
    public static class QueuesListener {

        private Duration pollTimeout = Duration.ofSeconds(5);

        private int maxPollMessages = 1;

        private Duration visibilityTimeout = Duration.ofSeconds(30);

        private boolean autoAck = false;

        private Duration errorBackoffInitial = Duration.ofSeconds(1);

        private Duration errorBackoffMax = Duration.ofSeconds(30);

        private double errorBackoffMultiplier = 2.0;

        public Duration getPollTimeout() {
            return pollTimeout;
        }

        public void setPollTimeout(Duration pollTimeout) {
            this.pollTimeout = pollTimeout;
        }

        public int getMaxPollMessages() {
            return maxPollMessages;
        }

        public void setMaxPollMessages(int maxPollMessages) {
            this.maxPollMessages = maxPollMessages;
        }

        public Duration getVisibilityTimeout() {
            return visibilityTimeout;
        }

        public void setVisibilityTimeout(Duration visibilityTimeout) {
            this.visibilityTimeout = visibilityTimeout;
        }

        public boolean isAutoAck() {
            return autoAck;
        }

        public void setAutoAck(boolean autoAck) {
            this.autoAck = autoAck;
        }

        public Duration getErrorBackoffInitial() {
            return errorBackoffInitial;
        }

        public void setErrorBackoffInitial(Duration errorBackoffInitial) {
            this.errorBackoffInitial = errorBackoffInitial;
        }

        public Duration getErrorBackoffMax() {
            return errorBackoffMax;
        }

        public void setErrorBackoffMax(Duration errorBackoffMax) {
            this.errorBackoffMax = errorBackoffMax;
        }

        public double getErrorBackoffMultiplier() {
            return errorBackoffMultiplier;
        }

        public void setErrorBackoffMultiplier(double errorBackoffMultiplier) {
            this.errorBackoffMultiplier = errorBackoffMultiplier;
        }
    }

    /**
     * Commands-specific listener settings.
     */
    public static class CommandsListener {

        private Duration timeout = Duration.ofSeconds(10);

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }
    }

    /**
     * Queries-specific listener settings.
     */
    public static class QueriesListener {

        private Duration timeout = Duration.ofSeconds(10);

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }
    }

    /**
     * Template (send-side) settings.
     */
    public static class Template {

        private boolean observationEnabled = true;

        public boolean isObservationEnabled() {
            return observationEnabled;
        }

        public void setObservationEnabled(boolean observationEnabled) {
            this.observationEnabled = observationEnabled;
        }
    }

    /**
     * Health indicator settings.
     */
    public static class Health {

        private boolean enabled = true;

        private Duration timeout = Duration.ofSeconds(5);

        private Duration cacheDuration = Duration.ofSeconds(15);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public Duration getCacheDuration() {
            return cacheDuration;
        }

        public void setCacheDuration(Duration cacheDuration) {
            this.cacheDuration = cacheDuration;
        }
    }

    /**
     * Micrometer metrics settings.
     */
    public static class Metrics {

        private boolean enabled = true;

        private Duration scrapeInterval = Duration.ofSeconds(30);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getScrapeInterval() {
            return scrapeInterval;
        }

        public void setScrapeInterval(Duration scrapeInterval) {
            this.scrapeInterval = scrapeInterval;
        }
    }

    /**
     * Kotlin coroutine settings for suspend-function listener support.
     */
    public static class Kotlin {

        /**
         * Coroutine dispatcher name: "default", "io", or "unconfined".
         */
        private String dispatcher = "default";

        public String getDispatcher() {
            return dispatcher;
        }

        public void setDispatcher(String dispatcher) {
            this.dispatcher = dispatcher;
        }
    }
}
