plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":kubemq-spring-boot-starter"))
    implementation(project(":kubemq-spring-cloud-stream-binder"))
    implementation(libs.spring.cloud.stream)
}

tasks.named("jar") {
    enabled = false
}
