package io.kubemq.spring.boot.kotlin

import io.kubemq.spring.boot.autoconfigure.KubeMQAutoConfiguration
import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties
import kotlinx.coroutines.Dispatchers
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.AnnotationAwareOrderComparator

/**
 * Auto-configuration for Kotlin-specific KubeMQ support.
 *
 * Activates when the Kotlin standard library and the autoconfigure module
 * are on the classpath. Provides:
 * - [KubeMQCoroutineListenerSupport] for invoking suspend listener methods
 * - [KubeMQConfigurerDsl] customization (if a user-defined bean is present)
 */
@AutoConfiguration
@AutoConfigureBefore(KubeMQAutoConfiguration::class)
@ConditionalOnClass(name = ["kotlin.coroutines.Continuation"])
@ConditionalOnProperty(prefix = "kubemq", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class KubeMQKotlinAutoConfiguration {

    private companion object {
        private val logger = LoggerFactory.getLogger(KubeMQKotlinAutoConfiguration::class.java)
    }

    @Bean
    @ConditionalOnMissingBean
    fun kubemqCoroutineListenerSupport(properties: KubeMQProperties): KubeMQCoroutineListenerSupport {
        val dispatcherName = properties.kotlin.dispatcher.trim().lowercase()
        val dispatcher = when (dispatcherName) {
            "io" -> Dispatchers.IO
            "unconfined" -> {
                logger.warn(
                    "kubemq.kotlin.dispatcher='unconfined' is active. Dispatchers.Unconfined does not " +
                        "guarantee thread-local propagation — SecurityContextHolder, MDC, and " +
                        "transaction context may not propagate correctly after suspend points."
                )
                Dispatchers.Unconfined
            }
            "default" -> Dispatchers.Default
            else -> {
                logger.warn(
                    "Unknown kubemq.kotlin.dispatcher='{}', falling back to Dispatchers.Default",
                    properties.kotlin.dispatcher
                )
                Dispatchers.Default
            }
        }
        return KubeMQCoroutineListenerSupport(dispatcher)
    }

    @Bean
    fun kubemqKotlinDslCustomizer(
        properties: KubeMQProperties,
        configurers: List<KubeMQConfigurerDsl>,
    ): KubeMQKotlinDslApplier = KubeMQKotlinDslApplier(properties, configurers)

    /**
     * Applies all [KubeMQConfigurerDsl] beans to [KubeMQProperties] on startup.
     *
     * When multiple [KubeMQConfigurerDsl] beans exist, they are applied in the order
     * determined by Spring's [org.springframework.core.annotation.Order] annotation
     * or [org.springframework.core.Ordered] interface. Lower values have higher priority.
     */
    class KubeMQKotlinDslApplier(
        properties: KubeMQProperties,
        configurers: List<KubeMQConfigurerDsl>,
    ) {
        init {
            val sorted = configurers.toMutableList()
            AnnotationAwareOrderComparator.sort(sorted)
            sorted.forEach { it.applyTo(properties) }
        }
    }
}
