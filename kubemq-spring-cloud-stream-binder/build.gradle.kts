plugins {
    `java-library`
}

dependencies {
    api(project(":kubemq-spring-boot-autoconfigure"))
    api(libs.spring.cloud.stream)
    implementation(libs.kubemq.sdk.java)

    testImplementation(libs.spring.boot.test)
    testImplementation(libs.grpc.testing)
    testImplementation(libs.grpc.inprocess)
    testImplementation(libs.awaitility)
}
