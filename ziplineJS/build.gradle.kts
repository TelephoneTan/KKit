plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.zipline)
}

kotlin {
    js(IR) {
        browser {}
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)
        }
    }
}

zipline {
    mainFunction = "main"
    val ziplineJSVersion: Int by rootProject.extra
    version = "$ziplineJSVersion"
    val ziplineJSPort: Int by rootProject.extra
    httpServerPort = ziplineJSPort
}
