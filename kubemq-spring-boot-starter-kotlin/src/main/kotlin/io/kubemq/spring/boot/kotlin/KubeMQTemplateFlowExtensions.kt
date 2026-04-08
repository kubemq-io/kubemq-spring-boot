package io.kubemq.spring.boot.kotlin

import io.kubemq.sdk.cq.CQClient
import io.kubemq.sdk.cq.CommandReceived
import io.kubemq.sdk.cq.QueryReceived
import io.kubemq.sdk.exception.KubeMQException
import io.kubemq.sdk.pubsub.EventMessageReceived
import io.kubemq.sdk.pubsub.PubSubClient
import io.kubemq.sdk.pubsub.StartPosition
import io.kubemq.sdk.queues.QueueReceivedMessage
import io.kubemq.sdk.queues.QueuesClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.coroutines.cancellation.CancellationException

private val logger = LoggerFactory.getLogger("io.kubemq.spring.boot.kotlin.KubeMQTemplateFlowExtensions")

/**
 * Kotlin [Flow] adapters for KubeMQ subscription patterns.
 *
 * Events and events-store flows delegate to the Kotlin SDK's [PubSubClient],
 * which already returns [Flow]. CQ flows delegate to the Kotlin SDK's [CQClient].
 * The queue polling flow wraps the Kotlin SDK's suspend receive in a
 * coroutine-friendly polling loop.
 */

/**
 * Returns a cold [Flow] of events from the given [channel].
 *
 * Cancelling the collecting coroutine automatically tears down the underlying
 * gRPC subscription stream.
 */
fun PubSubClient.eventsFlow(
    channel: String,
    group: String = "",
): Flow<EventMessageReceived> = subscribeToEvents {
    this.channel = channel
    this.group = group
}

/**
 * Returns a cold [Flow] of events-store messages from the given [channel].
 *
 * The Kotlin SDK uses [EventMessageReceived] for both events and events store;
 * store messages have a non-zero [EventMessageReceived.sequence].
 */
fun PubSubClient.eventsStoreFlow(
    channel: String,
    group: String = "",
    startPosition: StartPosition = StartPosition.StartNewOnly,
): Flow<EventMessageReceived> = subscribeToEventsStore {
    this.channel = channel
    this.group = group
    this.startPosition = startPosition
}

/**
 * Returns a cold [Flow] that polls a queue channel at the specified [pollInterval].
 *
 * Each poll uses the Kotlin SDK's suspend [QueuesClient.receiveQueuesMessages]
 * via the `QueuesDownstream` streaming RPC (`WaitTimeout` in milliseconds).
 * The flow emits individual [QueueReceivedMessage] items.
 * Cancelling the collecting coroutine stops the polling loop.
 *
 * Default values for [pollInterval] and [autoAck] are aligned with
 * `kubemq.listener.queues.pollTimeout` and `kubemq.listener.queues.autoAck`
 * properties from [io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties.Listener.QueuesListener].
 *
 * When [autoAck] is `false`, callers must explicitly call `ack()`, `reject()`,
 * or `reQueue()` on each [QueueReceivedMessage] to complete the transaction.
 *
 * @param channel the queue channel to poll
 * @param pollInterval time between polls (default: 5s, matching kubemq.listener.queues.pollTimeout)
 * @param maxMessages max messages per poll (default: 1)
 * @param autoAck whether to auto-acknowledge messages (default: false, matching kubemq.listener.queues.autoAck)
 * @param dispatcher the coroutine dispatcher for polling I/O (default: [Dispatchers.IO])
 */
fun QueuesClient.queuesFlow(
    channel: String,
    pollInterval: Duration = Duration.ofSeconds(5),
    maxMessages: Int = 1,
    autoAck: Boolean = false,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
): Flow<QueueReceivedMessage> = flow {
    val pollMs = pollInterval.toMillis()
    val waitTimeoutMs = pollMs.coerceIn(1, Int.MAX_VALUE.toLong()).toInt()
    var currentBackoffMs = pollMs
    val maxBackoffMs = pollMs * 32

    while (currentCoroutineContext().isActive) {
        try {
            val response = receiveQueuesMessages {
                this.channel = channel
                this.maxItems = maxMessages
                this.waitTimeoutMs = waitTimeoutMs
                this.autoAck = autoAck
            }
            if (response.isError) {
                logger.warn("Queue poll error on channel '{}': {}", channel, response.error)
                throw IllegalStateException(
                    "Queue poll error on channel '$channel': ${response.error}"
                )
            }
            for (msg in response.messages) {
                emit(msg)
            }
            currentBackoffMs = pollMs
        } catch (e: CancellationException) {
            throw e
        } catch (e: KubeMQException) {
            logger.warn(
                "KubeMQ error polling channel '{}': {} [retryable={}]",
                channel, e.message, e.isRetryable, e
            )
            if (!e.isRetryable) throw e
            delay(currentBackoffMs)
            currentBackoffMs = (currentBackoffMs * 2).coerceAtMost(maxBackoffMs)
            continue
        } catch (e: Exception) {
            logger.warn("Unexpected error polling queue channel '{}': {}", channel, e.message, e)
            delay(currentBackoffMs)
            currentBackoffMs = (currentBackoffMs * 2).coerceAtMost(maxBackoffMs)
            continue
        }
        delay(pollMs)
    }
}.flowOn(dispatcher)

/**
 * Returns a cold [Flow] of command requests from the given [channel].
 */
fun CQClient.commandsFlow(
    channel: String,
    group: String = "",
): Flow<CommandReceived> = subscribeToCommands {
    this.channel = channel
    this.group = group
}

/**
 * Returns a cold [Flow] of query requests from the given [channel].
 */
fun CQClient.queriesFlow(
    channel: String,
    group: String = "",
): Flow<QueryReceived> = subscribeToQueries {
    this.channel = channel
    this.group = group
}
