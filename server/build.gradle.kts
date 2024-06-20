plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "pub.telephone.kkit"
version = "1.0.0"
application {
    mainClass.set("pub.telephone.kkit.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)
    //
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.serialization.kotlinx.json.jvm)
    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.ktor.server.websockets.jvm)
    implementation(libs.ktor.server.swagger.jvm)
    implementation(libs.ktor.server.partial.content.jvm)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.default.headers.jvm)
    implementation(libs.ktor.server.cors.jvm)
    implementation(libs.ktor.server.conditional.headers.jvm)
    implementation(libs.ktor.server.compression.jvm)
    implementation(libs.ktor.server.caching.headers.jvm)
    implementation(libs.ktor.server.host.common.jvm)
    implementation(libs.ktor.server.status.pages.jvm)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.auto.head.response.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.tests.jvm)
    //
    implementation(libs.mongodb.driver.kotlin.coroutine)
    //
    implementation(libs.bson.kotlinx)
    implementation(libs.bson)
    //
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    //
    implementation(libs.mariadb.java.client)
    //
    implementation(libs.logback.classic)
}