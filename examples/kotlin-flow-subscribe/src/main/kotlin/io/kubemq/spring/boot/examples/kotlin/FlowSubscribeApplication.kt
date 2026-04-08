package io.kubemq.spring.boot.examples.kotlin

import io.kubemq.sdk.pubsub.PubSubClient
import io.kubemq.spring.boot.kotlin.sendEventSuspend
import io.kubemq.spring.boot.autoconfigure.template.KubeMQTemplate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

/**
 * Kotlin Flow Subscribe Example
 *
 * Demonstrates Kotlin-idiomatic event subscription with Kotlin Flow / coroutine-based subscription.
 */
@SpringBootApplication
class FlowSubscribeApplication

fun main(args: Array<String>) {
    runApplication<FlowSubscribeApplication>(*args)
}

@Component
class FlowSubscribeRunner(
    private val template: KubeMQTemplate,
    private val pubSubClient: PubSubClient
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(FlowSubscribeRunner::class.java)

    override fun run(args: ApplicationArguments) {
        runBlocking {
            try {
                val subJob: Job = launch {
                    try {
                        pubSubClient.subscribeToEvents {
                            channel = "spring-kotlin.flow-subscribe"
                        }.collect { event ->
                            val body = String(event.body)
                            log.info("Flow received: channel={} body={}", event.channel, body)
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        log.error("Subscription error: {}", e.message, e)
                    }
                }
                log.info("Kotlin flow subscription started")

                delay(500)
                for (i in 1..3) {
                    template.sendEventSuspend("spring-kotlin.flow-subscribe", "Kotlin flow event #$i")
                    log.info("Published flow event #{}", i)
                }
                delay(1000)

                subJob.cancel()
                subJob.join()
                log.info("Kotlin flow subscribe example completed.")
            } catch (ex: Exception) {
                log.warn("Could not run example (is KubeMQ running?): {}", ex.message)
            }
        }
    }
}

// Expected output:
// INFO  Kotlin flow subscription started
// INFO  Flow received: channel=spring-kotlin.flow-subscribe body=Kotlin flow event #1
// INFO  Published flow event #1
// INFO  Flow received: channel=spring-kotlin.flow-subscribe body=Kotlin flow event #2
// INFO  Published flow event #2
// INFO  Flow received: channel=spring-kotlin.flow-subscribe body=Kotlin flow event #3
// INFO  Published flow event #3
// INFO  Kotlin flow subscribe example completed.
