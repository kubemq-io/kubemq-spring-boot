package io.kubemq.spring.boot.kotlin

import io.kubemq.spring.boot.autoconfigure.converter.KubeMQMessageConverter

/**
 * Reified extension for [KubeMQMessageConverter] that eliminates the need
 * to pass `Class<T>` explicitly when deserializing message payloads.
 *
 * Returns `null` when [data] is null or empty, matching the behavior of
 * [io.kubemq.spring.boot.autoconfigure.converter.JacksonKubeMQMessageConverter.fromBytes].
 *
 * Example:
 * ```kotlin
 * val order: Order? = converter.fromBytes<Order>(message.body)
 * ```
 */
inline fun <reified T> KubeMQMessageConverter.fromBytes(data: ByteArray): T? =
    fromBytes(data, T::class.java)

/**
 * Reified extension that throws [IllegalStateException] when deserialization
 * returns null (i.e. when [data] is null or empty).
 *
 * Use this when a non-null result is expected and null would indicate a bug.
 *
 * Example:
 * ```kotlin
 * val order: Order = converter.fromBytesOrThrow<Order>(message.body)
 * ```
 */
inline fun <reified T> KubeMQMessageConverter.fromBytesOrThrow(data: ByteArray): T =
    fromBytes(data, T::class.java)
        ?: throw IllegalStateException(
            "Deserialization of ${T::class.java.name} returned null for ${data.size}-byte input"
        )
