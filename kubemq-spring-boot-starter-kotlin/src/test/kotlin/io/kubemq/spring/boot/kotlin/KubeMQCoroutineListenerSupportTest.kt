package io.kubemq.spring.boot.kotlin

import kotlinx.coroutines.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@OptIn(ExperimentalCoroutinesApi::class)
class KubeMQCoroutineListenerSupportTest {

    private lateinit var support: KubeMQCoroutineListenerSupport

    @BeforeEach
    fun setUp() {
        support = KubeMQCoroutineListenerSupport(Dispatchers.Unconfined)
    }

    @AfterEach
    fun tearDown() {
        support.destroy()
    }

    @Test
    fun isSuspendFunction_detectsSuspendFunction() {
        val method = TestSuspendHandler::class.java.methods
            .first { it.name == "handleMessage" }
        assertThat(support.isSuspendFunction(method)).isTrue()
    }

    @Test
    fun isSuspendFunction_detectsNonSuspendFunction() {
        val method = TestSuspendHandler::class.java.methods
            .first { it.name == "handleSync" }
        assertThat(support.isSuspendFunction(method)).isFalse()
    }

    @Test
    fun invokeSuspend_executesSuspendFunction() {
        val handler = TestSuspendHandler()
        val method = TestSuspendHandler::class.java.methods
            .first { it.name == "handleMessage" }

        val future = support.invokeSuspend("container-1", handler, method, "test-payload")
        val result = future.get(5, TimeUnit.SECONDS)

        assertThat(handler.invoked.get()).isTrue()
        assertThat(handler.receivedPayload.get()).isEqualTo("test-payload")
    }

    @Test
    fun scopeForContainer_isolatesScopes() {
        val scope1 = support.scopeForContainer("container-1")
        val scope2 = support.scopeForContainer("container-2")
        val scope1Again = support.scopeForContainer("container-1")

        assertThat(scope1).isSameAs(scope1Again)
        assertThat(scope1).isNotSameAs(scope2)
    }

    @Test
    fun cancelScope_cancelsContainerScope() {
        val scope = support.scopeForContainer("container-cancel")
        assertThat(scope.isActive).isTrue()

        support.cancelScope("container-cancel")

        // After cancel, a new scope is created for the same container
        val newScope = support.scopeForContainer("container-cancel")
        assertThat(newScope).isNotSameAs(scope)
        assertThat(newScope.isActive).isTrue()
    }

    @Test
    fun destroy_cancelsAllScopes() {
        val scope1 = support.scopeForContainer("container-d1")
        val scope2 = support.scopeForContainer("container-d2")

        assertThat(scope1.isActive).isTrue()
        assertThat(scope2.isActive).isTrue()

        support.destroy()

        // After destroy, scopes are cancelled
        assertThat(scope1.isActive).isFalse()
        assertThat(scope2.isActive).isFalse()
    }

    @Test
    fun configurable_dispatcher() {
        val dispatchers = listOf(
            Dispatchers.IO to "IO",
            Dispatchers.Unconfined to "Unconfined",
            Dispatchers.Default to "Default"
        )

        for ((dispatcher, name) in dispatchers) {
            val customSupport = KubeMQCoroutineListenerSupport(dispatcher)
            try {
                val scope = customSupport.scopeForContainer("test-$name")

                val contextDispatcher = scope.coroutineContext[CoroutineDispatcher]
                assertThat(contextDispatcher)
                    .describedAs("Dispatcher for $name")
                    .isEqualTo(dispatcher)

                val handler = TestSuspendHandler()
                val method = TestSuspendHandler::class.java.methods
                    .first { it.name == "handleMessage" }
                val future = customSupport.invokeSuspend("test-$name", handler, method, "payload-$name")
                future.get(5, TimeUnit.SECONDS)
                assertThat(handler.invoked.get()).isTrue()
            } finally {
                customSupport.destroy()
            }
        }
    }

    @Test
    fun kFunctionCache_repeatedCallsReturnConsistentResults() {
        val suspendMethod = TestSuspendHandler::class.java.methods
            .first { it.name == "handleMessage" }
        val syncMethod = TestSuspendHandler::class.java.methods
            .first { it.name == "handleSync" }

        // Call multiple times — should always return the same result (cached)
        repeat(10) {
            assertThat(support.isSuspendFunction(suspendMethod)).isTrue()
            assertThat(support.isSuspendFunction(syncMethod)).isFalse()
        }

        // invoke should also work repeatedly with caching
        val handler = TestSuspendHandler()
        repeat(5) { i ->
            val future = support.invokeSuspend("cache-test", handler, suspendMethod, "msg-$i")
            future.get(5, TimeUnit.SECONDS)
        }
        assertThat(handler.invoked.get()).isTrue()
    }

    // Test helper class
    class TestSuspendHandler {
        val invoked = AtomicBoolean(false)
        val receivedPayload = AtomicReference<String>()

        suspend fun handleMessage(payload: String): String? {
            invoked.set(true)
            receivedPayload.set(payload)
            return null
        }

        fun handleSync(payload: String) {
            invoked.set(true)
            receivedPayload.set(payload)
        }
    }
}
