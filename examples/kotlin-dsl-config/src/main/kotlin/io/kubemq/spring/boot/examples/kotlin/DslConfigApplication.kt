package io.kubemq.spring.boot.examples.kotlin

import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate
import kotlinx.coroutines.runBlocking
import io.kubemq.spring.boot.kotlin.sendEventSuspend
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

/**
 * Kotlin DSL Configuration Example
 *
 * Demonstrates Kotlin-idiomatic configuration and property injection.
 */
@SpringBootApplication
class DslConfigApplication

fun main(args: Array<String>) {
    runApplication<DslConfigApplication>(*args)
}

@Component
class DslConfigRunner(
    private val template: KubeMQTemplate,
    @Value("\${kubemq.address}") private val address: String,
    @Value("\${kubemq.client-id}") private val clientId: String
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(DslConfigRunner::class.java)

    override fun run(args: ApplicationArguments) {
        log.info("KubeMQ address: {}", address)
        log.info("KubeMQ client-id: {}", clientId)
        runBlocking {
            try {
                template.sendEventSuspend("spring-kotlin.dsl-config", "DSL config event")
                log.info("Sent event via Kotlin DSL config")
                log.info("Kotlin DSL config example completed.")
            } catch (ex: Exception) {
                log.warn("Could not run example (is KubeMQ running?): {}", ex.message)
            }
        }
    }
}

// Expected output:
// INFO  KubeMQ address: localhost:50000
// INFO  KubeMQ client-id: spring-kotlin-dsl-config
// INFO  Sent event via Kotlin DSL config
// INFO  Kotlin DSL config example completed.
