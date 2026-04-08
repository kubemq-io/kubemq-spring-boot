package io.kubemq.spring.boot.kotlin

import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KubeMQKotlinAutoConfigurationTest {

    private val config = KubeMQKotlinAutoConfiguration()

    @Test
    fun dispatcher_io_resolvedCorrectly() {
        val props = propertiesWithDispatcher("io")
        val support = config.kubemqCoroutineListenerSupport(props)
        try {
            val scope = support.scopeForContainer("test")
            assertThat(scope.coroutineContext[CoroutineDispatcher]).isEqualTo(Dispatchers.IO)
        } finally {
            support.destroy()
        }
    }

    @Test
    fun dispatcher_default_resolvedCorrectly() {
        val props = propertiesWithDispatcher("default")
        val support = config.kubemqCoroutineListenerSupport(props)
        try {
            val scope = support.scopeForContainer("test")
            assertThat(scope.coroutineContext[CoroutineDispatcher]).isEqualTo(Dispatchers.Default)
        } finally {
            support.destroy()
        }
    }

    @Test
    fun dispatcher_unconfined_resolvedCorrectly() {
        val props = propertiesWithDispatcher("unconfined")
        val support = config.kubemqCoroutineListenerSupport(props)
        try {
            val scope = support.scopeForContainer("test")
            assertThat(scope.coroutineContext[CoroutineDispatcher]).isEqualTo(Dispatchers.Unconfined)
        } finally {
            support.destroy()
        }
    }

    @Test
    fun dispatcher_trimAndLowercase() {
        // " IO " with leading/trailing whitespace and uppercase should resolve to Dispatchers.IO
        val props = propertiesWithDispatcher("  IO  ")
        val support = config.kubemqCoroutineListenerSupport(props)
        try {
            val scope = support.scopeForContainer("test")
            assertThat(scope.coroutineContext[CoroutineDispatcher]).isEqualTo(Dispatchers.IO)
        } finally {
            support.destroy()
        }
    }

    @Test
    fun dispatcher_unknownFallsBackToDefault() {
        val props = propertiesWithDispatcher("typo-value")
        val support = config.kubemqCoroutineListenerSupport(props)
        try {
            val scope = support.scopeForContainer("test")
            assertThat(scope.coroutineContext[CoroutineDispatcher]).isEqualTo(Dispatchers.Default)
        } finally {
            support.destroy()
        }
    }

    @Test
    fun dslApplier_sortsMutableCopy() {
        // Verify that the applier applies all configurers and that the list order
        // determines which configurer wins (last-write-wins for same property).
        // The sort is based on AnnotationAwareOrderComparator which uses @Order/Ordered
        // on the bean instances — since KubeMQConfigurerDsl doesn't implement Ordered,
        // un-annotated beans apply in their original list order.
        val props = KubeMQProperties()
        val first = kubemq { address = "first:50000" }
        val second = kubemq { address = "second:50000" }

        // second is last in the list so it wins (last-write-wins)
        KubeMQKotlinAutoConfiguration.KubeMQKotlinDslApplier(props, listOf(first, second))
        assertThat(props.address).isEqualTo("second:50000")
    }

    @Test
    fun dslApplier_appliesConfiguration() {
        val props = KubeMQProperties()
        val configurer = kubemq {
            address = "test-broker:50000"
            clientId = "my-app"
            authToken = "secret"
        }

        KubeMQKotlinAutoConfiguration.KubeMQKotlinDslApplier(props, listOf(configurer))

        assertThat(props.address).isEqualTo("test-broker:50000")
        assertThat(props.clientId).isEqualTo("my-app")
        assertThat(props.authToken).isEqualTo("secret")
    }

    private fun propertiesWithDispatcher(dispatcher: String): KubeMQProperties {
        val props = KubeMQProperties()
        props.kotlin.dispatcher = dispatcher
        return props
    }
}
