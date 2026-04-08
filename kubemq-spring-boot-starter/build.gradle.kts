plugins {
    `java-library`
}

dependencies {
    api(project(":kubemq-spring-boot-autoconfigure"))
    api(libs.kubemq.sdk.java)
    api(libs.spring.boot.starter)
}
