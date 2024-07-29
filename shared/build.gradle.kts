import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.swiftKlib)
    alias(libs.plugins.kotlinPluginSerialization)
}

kotlin {
    js(IR) {
        browser {}
        nodejs {}
        binaries.executable()
    }
    
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.compilations {
            val main by getting {
                cinterops {
                    create("ios")
                }
            }
        }
    }
    
    jvm()

    applyDefaultHierarchyTemplate()
    
    sourceSets {
        commonMain.get().apply {
            dependencies {
                api(libs.kotlinx.datetime)
                implementation(libs.cryptography.core)
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.coroutines.core)
                api(libs.ktor.client.core)
                api(libs.ktor.client.websockets)
                api(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ksoup)
            }
            buildConfig {
                useKotlinOutput { internalVisibility = false }
                //
                val webSocketServerScheme: String by rootProject.extra
                buildConfigField<String>("WEB_SOCKET_SERVER_SCHEME", webSocketServerScheme)
                //
                val webSocketServerHost: String by rootProject.extra
                buildConfigField<String>("WEB_SOCKET_SERVER_HOST", webSocketServerHost)
                //
                val webSocketServerPort: Int by rootProject.extra
                buildConfigField<Int>("WEB_SOCKET_SERVER_PORT", webSocketServerPort)
                //
                val serverScheme: String by rootProject.extra
                buildConfigField<String>("SERVER_SCHEME", serverScheme)
                //
                val serverHost: String by rootProject.extra
                buildConfigField<String>("SERVER_HOST", serverHost)
                //
                val serverPort: Int by rootProject.extra
                buildConfigField<Int>("SERVER_PORT", serverPort)
                //
                val cdnOrigin: String by rootProject.extra
                buildConfigField<String>("CDN_ORIGIN", cdnOrigin)
                //
                val cdnHost: Array<String> by rootProject.extra
                buildConfigField<Array<String>>("CDN_HOST", cdnHost)
            }
        }
        commonTest.get().apply {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val localClient by creating {
            dependsOn(commonMain.get())
            dependencies {
                api(libs.compose.webview.multiplatform)
            }
        }
        androidMain.get().apply {
            dependsOn(localClient)
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
        jvmMain.get().apply {
            dependsOn(localClient)
            dependencies {
                implementation(libs.cryptography.provider.jdk)
                implementation(libs.ktor.client.okhttp)
            }
        }
        jvmTest.get().apply {
            dependencies {
                implementation(libs.kotlin.test.junit)
            }
        }
        iosMain.get().apply {
            dependsOn(localClient)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        jsMain.get().apply {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
    }
}

android {
    namespace = "pub.telephone.kkit.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

swiftklib {
    create("ios") {
        path = file("../ios/Sources/ios")
        packageName("")
    }
}
