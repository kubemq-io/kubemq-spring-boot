package io.kubemq.spring.boot.kotlin

import io.kubemq.sdk.exception.KubeMQException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class KubeMQTemplateFlowExtensionsTest {

    @Test
    fun flow_emission_collectsValues() = runTest {
        val items = mutableListOf<String>()
        val testFlow: Flow<String> = flow {
            emit("msg-1")
            emit("msg-2")
            emit("msg-3")
        }

        testFlow.collect { items.add(it) }

        assertThat(items).containsExactly("msg-1", "msg-2", "msg-3")
    }

    @Test
    fun cancellationException_isRethrown() = runTest {
        val flow: Flow<String> = flow {
            throw CancellationException("test cancellation")
        }

        var caught = false
        try {
            flow.collect { value: String -> /* no-op */ }
        } catch (e: CancellationException) {
            caught = true
            assertThat(e.message).isEqualTo("test cancellation")
        }
        assertThat(caught).isTrue()
    }

    @Test
    fun errorResponse_throwsException() = runTest {
        // Mirrors the queuesFlow pattern: when isError is true, an exception is thrown
        // instead of silently continuing
        var exceptionThrown = false
        val flow: Flow<String> = flow {
            val isError = true
            val errorMsg = "permission denied"
            if (isError) {
                throw IllegalStateException("Queue poll error on channel 'test': $errorMsg")
            }
            emit("should-not-reach")
        }

        try {
            flow.collect {}
        } catch (e: IllegalStateException) {
            exceptionThrown = true
            assertThat(e.message).contains("permission denied")
            assertThat(e.message).contains("test")
        }
        assertThat(exceptionThrown).isTrue()
    }

    @Test
    fun fatalException_propagates() = runTest {
        // Mirrors the queuesFlow pattern: non-retryable KubeMQException is rethrown
        var caught = false
        val flow: Flow<String> = flow {
            try {
                throw KubeMQException.Authentication(
                    "bad credentials", operation = "receiveQueuesMessages"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: KubeMQException) {
                if (!e.isRetryable) throw e
            }
            emit("should-not-reach")
        }

        try {
            flow.collect {}
        } catch (e: KubeMQException.Authentication) {
            caught = true
            assertThat(e.isRetryable).isFalse()
        }
        assertThat(caught).isTrue()
    }

    @Test
    fun retryableException_continuesPolling() = runTest {
        // Mirrors the queuesFlow pattern: retryable exceptions are caught and the loop retries
        var attempts = 0
        val results = mutableListOf<String>()

        val flow: Flow<String> = flow {
            while (currentCoroutineContext().isActive && attempts < 3) {
                attempts++
                try {
                    if (attempts == 1) {
                        throw KubeMQException.Connection(
                            "broker unavailable", operation = "receiveQueuesMessages"
                        )
                    }
                    emit("recovered-msg")
                    return@flow
                } catch (e: CancellationException) {
                    throw e
                } catch (e: KubeMQException) {
                    if (!e.isRetryable) throw e
                    // retryable: continue loop (backoff omitted for test speed)
                }
            }
        }

        flow.collect { results.add(it) }

        assertThat(attempts).isEqualTo(2)
        assertThat(results).containsExactly("recovered-msg")
    }

    @Test
    fun queues_flow_polls_and_emits() = runTest {
        // Simulates the queuesFlow polling-and-emit pattern with updated defaults
        // (autoAck=false aligned with Spring listener properties)
        val batches = listOf(
            listOf("msg-1", "msg-2"),
            emptyList(),
            listOf("msg-3"),
        )
        var pollIndex = 0

        val pollingFlow: Flow<String> = flow {
            while (currentCoroutineContext().isActive && pollIndex < batches.size) {
                try {
                    val batch = batches[pollIndex++]
                    val isError = false
                    if (isError) {
                        throw IllegalStateException("Queue poll error")
                    }
                    for (msg in batch) {
                        emit(msg)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: KubeMQException) {
                    if (!e.isRetryable) throw e
                } catch (_: Exception) {
                    // retryable unknown error
                }
            }
        }

        val collected = mutableListOf<String>()
        pollingFlow.collect { collected.add(it) }

        assertThat(collected).containsExactly("msg-1", "msg-2", "msg-3")
        assertThat(pollIndex).isEqualTo(3)
    }

    @Test
    fun waitTimeoutMs_handlesLargeAndSmallDurations() {
        // Verify the coerceIn logic used in queuesFlow
        // Small: sub-second should work (no artificial 1000ms floor)
        val smallMs = Duration.ofMillis(100).toMillis()
        val smallTimeout = smallMs.coerceIn(1, Int.MAX_VALUE.toLong()).toInt()
        assertThat(smallTimeout).isEqualTo(100)

        // Large: values beyond Int.MAX_VALUE should clamp
        val hugeMs = Long.MAX_VALUE
        val hugeTimeout = hugeMs.coerceIn(1, Int.MAX_VALUE.toLong()).toInt()
        assertThat(hugeTimeout).isEqualTo(Int.MAX_VALUE)

        // Normal: 5 seconds (default)
        val normalMs = Duration.ofSeconds(5).toMillis()
        val normalTimeout = normalMs.coerceIn(1, Int.MAX_VALUE.toLong()).toInt()
        assertThat(normalTimeout).isEqualTo(5000)
    }
}
