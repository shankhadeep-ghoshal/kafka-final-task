import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.spring") version "1.7.20"
}

group = "com.shankhadeepghoshal"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
}

val mockkVersion = "1.13.2"
val springMockk = "3.1.1"
val r2dbcDriver = "0.8.2.RELEASE"

dependencies {
    // Spring stuff
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    // Jetbrains/Kotlin specific stuff
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Persistence
    implementation("io.r2dbc:r2dbc-spi")
    runtimeOnly("mysql:mysql-connector-java")
    runtimeOnly("dev.miku:r2dbc-mysql:${r2dbcDriver}")

    // Test stuff
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.ninja-squad:springmockk:${springMockk}")
    testImplementation("io.mockk:mockk:${mockkVersion}")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("io.r2dbc:r2dbc-h2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
