plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":kubemq-spring-boot-starter"))
}

tasks.named("jar") {
    enabled = false
}
