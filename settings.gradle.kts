pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("org.springframework.boot") version "3.2.0"
        id("io.spring.dependency-management") version "1.1.7"
        kotlin("jvm") version "2.1.20"
    }
}

rootProject.name = "kubemq-spring-boot"

include(
    "kubemq-spring-boot-autoconfigure",
    "kubemq-spring-boot-starter",
    "kubemq-spring-cloud-stream-binder",
    "kubemq-spring-boot-starter-kotlin",
    "kubemq-spring-boot-starter-test",
    "examples:events-basic-pubsub",
    "examples:events-wildcard-subscribe",
    "examples:events-consumer-group",
    "examples:events-stream-send",
    "examples:events-cancel-subscription",
    "examples:events-multiple-subscribers",
    "examples:events-store-basic",
    "examples:events-store-start-new-only",
    "examples:events-store-start-from-first",
    "examples:events-store-start-from-last",
    "examples:events-store-replay-sequence",
    "examples:events-store-replay-time",
    "examples:events-store-time-delta",
    "examples:events-store-consumer-group",
    "examples:events-store-stream-send",
    "examples:events-store-cancel-subscription",
    "examples:commands-send",
    "examples:commands-handle",
    "examples:commands-consumer-group",
    "examples:commands-timeout",
    "examples:queries-send",
    "examples:queries-handle",
    "examples:queries-cached",
    "examples:queries-consumer-group",
    "examples:queues-send-receive",
    "examples:queues-batch-send",
    "examples:queues-peek",
    "examples:queues-delayed",
    "examples:queues-expiration",
    "examples:queues-dead-letter",
    "examples:queues-ack-all",
    "examples:queues-ack-reject",
    "examples:autoconfigure-profiles",
    "examples:template-fluent-builders",
    "examples:spring-cloud-stream-events",
    "examples:spring-cloud-stream-queues",
    "examples:spring-cloud-stream-events-store",
    "examples:kotlin-coroutine-publish",
    "examples:kotlin-flow-subscribe",
    "examples:kotlin-dsl-config",
    "examples:health-actuator",
    "examples:observability-micrometer",
    "examples:connection-ping",
    "examples:connection-tls",
    "examples:connection-mtls",
    "examples:connection-auth-token",
    "examples:error-handling-reconnection",
    "examples:error-handling-graceful-shutdown",
    "examples:management-create-channel",
    "examples:management-delete-channel",
    "examples:management-list-channels",
    "examples:management-purge-queue"
)
