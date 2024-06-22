import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.zipline)
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

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.get().apply {
            dependencies {
                api(projects.shared)
            }
        }
        val ziplineMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                api(libs.zipline)
            }
        }
        jsMain.get().apply {
            dependsOn(ziplineMain)
        }
        val ziplineHostMain by creating {
            dependsOn(ziplineMain)
            dependencies {
                implementation(libs.zipline.loader)
            }
        }
        val ziplineJavaHostMain by creating {
            dependsOn(ziplineHostMain)
            dependencies {
                implementation(libs.okhttp3)
            }
        }
        androidMain.get().apply {
            dependsOn(ziplineJavaHostMain)
        }
        iosMain.get().apply {
            dependsOn(ziplineHostMain)
        }
        jvmMain.get().apply {
            dependsOn(ziplineJavaHostMain)
        }
    }
}

android {
    namespace = "pub.telephone.kkit.sharedZipline"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
