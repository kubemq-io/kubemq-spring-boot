plugins {
    `java-library`
}

dependencies {
    api(libs.spring.boot.autoconfigure)
    api(libs.kubemq.sdk.java)

    compileOnly(libs.spring.boot.actuator)
    compileOnly(libs.micrometer.core)
    compileOnly(libs.micrometer.observation)
    compileOnly(libs.spring.cloud.stream)

    annotationProcessor(libs.spring.boot.configuration.processor)

    testImplementation(libs.spring.boot.test)
    testImplementation(libs.spring.boot.actuator)
    testImplementation(libs.micrometer.core)
    testImplementation(libs.micrometer.observation)
}
