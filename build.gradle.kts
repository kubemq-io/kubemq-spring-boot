import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

plugins {
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
}

group = "io.kubemq"
version = "1.0.0"

subprojects {
    val isLibrary = !project.path.startsWith(":examples:")

    if (isLibrary) {
        apply(plugin = "java-library")
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
    }
    apply(plugin = "io.spring.dependency-management")

    group = "io.kubemq"
    version = "1.0.0"

    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
            if (isLibrary) {
                // Maven Central requires -sources and -javadoc jars for every artifact.
                withSourcesJar()
                withJavadocJar()
            }
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

    if (isLibrary) {
        // Publish to Maven Central via the Sonatype Central Portal.
        // Credentials and the signing key are supplied at release time through gradle
        // properties or environment variables; when absent (e.g. local development),
        // signing is skipped and `publishToMavenLocal` still works.
        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])
                    pom {
                        name.set(project.name)
                        description.set("KubeMQ Spring Boot integration — ${project.name}")
                        url.set("https://github.com/kubemq-io/kubemq-spring-boot")
                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                id.set("kubemq")
                                name.set("KubeMQ")
                                email.set("info@kubemq.io")
                            }
                        }
                        scm {
                            connection.set("scm:git:https://github.com/kubemq-io/kubemq-spring-boot.git")
                            developerConnection.set("scm:git:ssh://git@github.com:kubemq-io/kubemq-spring-boot.git")
                            url.set("https://github.com/kubemq-io/kubemq-spring-boot")
                        }
                    }
                }
            }
            repositories {
                maven {
                    name = "centralPortal"
                    val releaseUrl =
                        uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                    val snapshotUrl =
                        uri("https://central.sonatype.com/repository/maven-snapshots/")
                    url = if (version.toString().endsWith("SNAPSHOT")) snapshotUrl else releaseUrl
                    credentials {
                        username = (findProperty("centralUsername") as String?) ?: System.getenv("CENTRAL_USERNAME")
                        password = (findProperty("centralPassword") as String?) ?: System.getenv("CENTRAL_PASSWORD")
                    }
                }
            }
        }

        configure<SigningExtension> {
            val signingKey = (findProperty("signingKey") as String?) ?: System.getenv("SIGNING_KEY")
            val signingPassword = (findProperty("signingPassword") as String?) ?: System.getenv("SIGNING_PASSWORD")
            if (signingKey != null) {
                useInMemoryPgpKeys(signingKey, signingPassword)
                sign(the<PublishingExtension>().publications["mavenJava"])
            }
        }
    }
}
