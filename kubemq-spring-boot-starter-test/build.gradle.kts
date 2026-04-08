plugins {
    `java-library`
}

dependencies {
    api(project(":kubemq-spring-boot-autoconfigure"))
    api(libs.spring.boot.test)
    api(libs.testcontainers)
    api(libs.grpc.testing)
    api(libs.grpc.inprocess)
    api(libs.awaitility)
}
