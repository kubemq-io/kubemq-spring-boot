plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(project(":kubemq-spring-boot-autoconfigure"))
    // The Kotlin SDK provides coroutine-native APIs (suspend functions, Flow) used by
    // the extension functions in this module. The Java SDK is transitively included via
    // the autoconfigure module and is required for template, listener, health, and metrics
    // beans. Both JARs share overlapping class names in io.kubemq.sdk.* but each provides
    // unique types needed by their respective consumers.
    api(libs.kubemq.sdk.kotlin)
    implementation(libs.kotlinx.coroutines.core)
    implementation(kotlin("reflect"))

    testImplementation(libs.spring.boot.test)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
