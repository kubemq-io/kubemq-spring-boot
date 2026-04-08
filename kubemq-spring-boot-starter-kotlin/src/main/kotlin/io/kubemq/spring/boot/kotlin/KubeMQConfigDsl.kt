package io.kubemq.spring.boot.kotlin

import io.kubemq.spring.boot.autoconfigure.properties.KubeMQProperties
import java.time.Duration

/**
 * DSL marker preventing accidental access to outer receiver scopes
 * when using nested KubeMQ configuration builders.
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class KubeMQSpringDsl

/**
 * DSL builder for configuring [KubeMQProperties] programmatically in Kotlin.
 *
 * Example:
 * ```kotlin
 * @Bean
 * fun kubemqConfigurer(): KubeMQConfigurerDsl = kubemq {
 *     address = "broker.example.com:50000"
 *     clientId = "my-service"
 *     tls {
 *         enabled = true
 *         certFile = "/certs/client.pem"
 *         keyFile = "/certs/client-key.pem"
 *     }
 *     connection {
 *         timeout = Duration.ofSeconds(15)
 *     }
 * }
 * ```
 */
@KubeMQSpringDsl
class KubeMQConfigurerDsl internal constructor() {

    var address: String = "localhost:50000"
    var clientId: String = ""
    var authToken: String = ""
    var enabled: Boolean = true

    private var tlsBlock: (TlsDsl.() -> Unit)? = null
    private var connectionBlock: (ConnectionDsl.() -> Unit)? = null

    fun tls(block: TlsDsl.() -> Unit) {
        tlsBlock = block
    }

    fun connection(block: ConnectionDsl.() -> Unit) {
        connectionBlock = block
    }

    /**
     * Applies the DSL configuration to a [KubeMQProperties] instance.
     */
    fun applyTo(properties: KubeMQProperties) {
        properties.isEnabled = enabled
        properties.address = address
        properties.clientId = clientId
        properties.authToken = authToken

        tlsBlock?.let { block ->
            val dsl = TlsDsl().apply(block)
            properties.tls.isEnabled = dsl.enabled
            properties.tls.certFile = dsl.certFile
            properties.tls.keyFile = dsl.keyFile
            properties.tls.caCertFile = dsl.caCertFile
        }

        connectionBlock?.let { block ->
            val dsl = ConnectionDsl().apply(block)
            properties.connection.timeout = dsl.timeout
            properties.connection.maxReceiveSize = dsl.maxReceiveSize
            properties.connection.keepAlive.time = dsl.keepAliveTime
            properties.connection.keepAlive.timeout = dsl.keepAliveTimeout
        }
    }
}

/**
 * TLS configuration DSL block.
 */
@KubeMQSpringDsl
class TlsDsl internal constructor() {
    var enabled: Boolean = false
    var certFile: String = ""
    var keyFile: String = ""
    var caCertFile: String = ""
}

/**
 * Connection tuning DSL block.
 */
@KubeMQSpringDsl
class ConnectionDsl internal constructor() {
    var timeout: Duration = Duration.ofSeconds(30)
    var maxReceiveSize: org.springframework.util.unit.DataSize =
        org.springframework.util.unit.DataSize.ofMegabytes(100)
    var keepAliveTime: Duration = Duration.ofSeconds(30)
    var keepAliveTimeout: Duration = Duration.ofSeconds(10)
}

/**
 * Entry point for the KubeMQ Spring Boot configuration DSL.
 *
 * Returns a [KubeMQConfigurerDsl] that can be exposed as a Spring bean
 * to customize [KubeMQProperties] programmatically.
 */
fun kubemq(block: KubeMQConfigurerDsl.() -> Unit): KubeMQConfigurerDsl =
    KubeMQConfigurerDsl().apply(block)
