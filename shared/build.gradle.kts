import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.swiftKlib)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.projectDir.path)
                    }
                }
            }
        }
    }

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
                implementation(libs.kotlinx.datetime)
                implementation(libs.cryptography.core)
            }
            buildConfig {
                useKotlinOutput { internalVisibility = false }
                //
                val serverPort: Int by rootProject.extra
                buildConfigField<Int>("SERVER_PORT", serverPort)
                //
                val serverHTTPBase: String by rootProject.extra
                buildConfigField<String>("SERVER_HTTP_BASE", serverHTTPBase)
                //
                val ziplineJSVersion: Int by rootProject.extra
                buildConfigField<Int>("ZIPLINE_JS_VERSION", ziplineJSVersion)
                //
                val ziplineJSPort: Int by rootProject.extra
                buildConfigField<Int>("ZIPLINE_JS_PORT", ziplineJSPort)
                //
                val cdnOrigin: String by rootProject.extra
                buildConfigField<String>("CDN_ORIGIN", cdnOrigin)
                //
                val cdnHost: Array<String> by rootProject.extra
                buildConfigField<Array<String>>("CDN_HOST", cdnHost)
            }
        }
        jvmMain.get().apply {
            dependencies {
                implementation(libs.cryptography.provider.jdk)
            }
        }
        jvmTest.get().apply {
            dependencies {
                implementation(libs.kotlin.test.junit)
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
