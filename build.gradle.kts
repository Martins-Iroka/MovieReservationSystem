plugins {
    alias(libs.plugins.flyway)
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

group = "com.martdev"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.server.callId)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.cors)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.openapi)
    implementation(ktorLibs.server.rateLimit)
    implementation(ktorLibs.server.requestValidation)
    implementation(ktorLibs.server.routingOpenapi)
    implementation(ktorLibs.server.statusPages)
    implementation(ktorLibs.server.swagger)
    implementation(libs.bcrypt)
    implementation(libs.dotenv)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.datetime)
    implementation(libs.exposed.jdbc)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)
    implementation(libs.hikariCP)
    implementation(libs.koin.annotation)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger)
    implementation(libs.logback.classic)
    implementation(libs.postgresql)
    implementation(libs.stytch)
    implementation(libs.testcontainers.postgresql)

    ksp(libs.koin.annotation.compiler)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.client.contentNegotiation)
    testImplementation(ktorLibs.server.testHost)
    testImplementation(libs.junit)
    testImplementation(libs.junit5)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit)
    testImplementation(libs.mockK)
    testImplementation(libs.testcontainers.junit5)
}

tasks.test {
    useJUnitPlatform()
}
