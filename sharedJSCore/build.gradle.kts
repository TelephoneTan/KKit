import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
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
        val jsCoreMain by creating {
            dependsOn(commonMain.get())
            dependencies {
            }
        }
        jsMain.get().apply {
            dependsOn(jsCoreMain)
        }
        val jsCoreHostMain by creating {
            dependsOn(jsCoreMain)
            dependencies {
                implementation(libs.quickjs.kt)
            }
        }
        androidMain.get().apply {
            dependsOn(jsCoreHostMain)
            dependencies {
            }
        }
        iosMain.get().apply {
            dependsOn(jsCoreHostMain)
            dependencies {
            }
        }
        jvmMain.get().apply {
            dependsOn(jsCoreHostMain)
            dependencies {
            }
        }
    }
}

android {
    namespace = "pub.telephone.kkit.sharedJSCore"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
