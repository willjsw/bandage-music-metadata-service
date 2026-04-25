plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "7.2.1"
    kotlin("plugin.jpa") version "2.2.21"
    kotlin("kapt") version "2.2.21"
}

group = "com.bandage"
version = "0.0.1-SNAPSHOT"
description = "music-metadata-service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")
    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.h2database:h2")
    //querydsl
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")
    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")
    // redis
//    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    // UUIDv7
    implementation("com.github.f4b6a3:uuid-creator:6.0.0")

    //Korutin
    implementation("io.ktor:ktor-client-core:2.3.x")
    implementation("io.ktor:ktor-client-cio:2.3.x") // 비동기 엔진
    implementation("io.ktor:ktor-client-content-negotiation:2.3.x")
    implementation("io.ktor:ktor-serialization-jackson:2.3.x") // JSON 파싱
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
