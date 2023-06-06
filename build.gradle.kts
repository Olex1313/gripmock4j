import com.google.protobuf.gradle.id

plugins {
    idea
    id("java")
    id("com.google.protobuf") version "0.9.3"
}

group = "org.github.olex.gripmock4j"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

protobuf {
    protoc {
        artifact = if (osdetector.os == "osx") {
            "com.google.protobuf:protoc:3.20.1:osx-aarch_64"
        } else {
            "com.google.protobuf:protoc:3.20.1"
        }
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.55.1"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc") { }
            }
        }
    }
}

sourceSets {
    main {
        proto {
            setSrcDirs(listOf("src/main/resources/proto"))
            include("*.proto")
        }
    }
    create("integrationTest") {
        java {
            setSrcDirs(listOf("src/integrationTest/java"))
        }
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.testImplementation.get())
}

configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter("test")

    useJUnitPlatform()

    testLogging {
        events("passed")
    }
}

tasks.check { dependsOn(integrationTest) }

idea {
    module {
        testSources.from(sourceSets["integrationTest"].java.srcDirs)
    }
}

dependencies {
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.7")

    // JSON
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")

    // gRPC
    runtimeOnly("io.grpc:grpc-netty-shaded:1.54.1")
    implementation("com.google.protobuf:protobuf-java:3.23.1")
    implementation("com.google.protobuf:protobuf-java-util:3.23.1")
    implementation("io.grpc:grpc-protobuf:1.54.1")
    implementation("io.grpc:grpc-stub:1.54.1")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53") // necessary for Java 9+

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.24.2")

    // Integration testing
    integrationTestImplementation("org.testcontainers:testcontainers:1.18.1")
    integrationTestImplementation("org.testcontainers:junit-jupiter:1.18.1")
}

tasks.test {
    useJUnitPlatform()
}