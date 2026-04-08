package io.kubemq.spring.boot.examples.kotlin

import io.kubemq.spring.boot.kotlin.sendEventSuspend
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

/**
 * Kotlin Coroutine Publish Example
 *
 * Demonstrates Kotlin suspend-function publishing with KubeMQTemplate.
 */
@SpringBootApplication
class CoroutinePublishApplication

fun main(args: Array<String>) {
    runApplication<CoroutinePublishApplication>(*args)
}

@Component
class CoroutinePublishRunner(private val template: KubeMQTemplate) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(CoroutinePublishRunner::class.java)

    override fun run(args: ApplicationArguments) {
        runBlocking {
            try {
                for (i in 1..5) {
                    template.sendEventSuspend("spring-kotlin.coroutine-publish", "Coroutine event #$i")
                    log.info("Published coroutine event #{}", i)
                }
                log.info("Kotlin coroutine publish example completed.")
            } catch (ex: Exception) {
                log.warn("Could not run example (is KubeMQ running?): {}", ex.message)
            }
        }
    }
}

// Expected output:
// INFO  Published coroutine event #1
// INFO  Published coroutine event #2
// ...
// INFO  Published coroutine event #5
// INFO  Kotlin coroutine publish example completed.
