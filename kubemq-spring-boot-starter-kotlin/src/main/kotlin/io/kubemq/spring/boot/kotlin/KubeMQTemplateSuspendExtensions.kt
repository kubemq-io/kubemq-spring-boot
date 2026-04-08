package io.kubemq.spring.boot.kotlin

import io.kubemq.sdk.cq.CommandResponseMessage
import io.kubemq.sdk.cq.QueryResponseMessage
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate
import kotlinx.coroutines.future.await
import java.time.Duration

/**
 * Suspend extension functions for [KubeMQTemplate] that bridge
 * `CompletableFuture`-based async methods to Kotlin coroutines.
 *
 * Each function delegates to the corresponding `*Async` method on the Java
 * template and suspends until the future completes, respecting structured
 * concurrency cancellation.
 */

suspend fun KubeMQTemplate.sendEventSuspend(channel: String, data: Any) {
    sendEventAsync(channel, data).await()
}

suspend fun KubeMQTemplate.sendEventSuspend(
    channel: String,
    data: Any,
    tags: Map<String, String>,
) {
    sendEventAsync(channel, data, tags).await()
}

suspend fun KubeMQTemplate.sendEventStoreSuspend(channel: String, data: Any) {
    sendEventStoreAsync(channel, data).await()
}

suspend fun KubeMQTemplate.sendEventStoreSuspend(
    channel: String,
    data: Any,
    tags: Map<String, String>,
) {
    sendEventStoreAsync(channel, data, tags).await()
}

suspend fun KubeMQTemplate.sendQueueMessageSuspend(channel: String, data: Any) {
    sendQueueMessageAsync(channel, data).await()
}

suspend fun KubeMQTemplate.sendQueueMessageSuspend(
    channel: String,
    data: Any,
    tags: Map<String, String>,
) {
    sendQueueMessageAsync(channel, data, tags).await()
}

suspend fun KubeMQTemplate.sendCommandSuspend(
    channel: String,
    data: Any,
    timeout: Duration,
): CommandResponseMessage {
    return sendCommandAsync(channel, data, timeout).await()
}

suspend fun KubeMQTemplate.sendQuerySuspend(
    channel: String,
    data: Any,
    timeout: Duration,
): QueryResponseMessage {
    return sendQueryAsync(channel, data, timeout).await()
}
