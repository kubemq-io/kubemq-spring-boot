plugins {
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
}

group = "io.kubemq"
version = "1.0.0"

subprojects {
    if (!project.path.startsWith(":examples:")) {
        apply(plugin = "java-library")
    }
    apply(plugin = "io.spring.dependency-management")

    group = "io.kubemq"
    version = "1.0.0"

    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    repositories {
        mavenCentral()
    }

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.0")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.0")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
