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
            implementation(projects.sharedZipline)
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

tasks.register("deployZiplineOnServer") {
    dependsOn("compileProductionExecutableKotlinJsZipline")
    doLast {
        val ziplineJSVersion: Int by rootProject.extra
        val basePath = project.projectDir.absolutePath
        val from = File("$basePath/build/compileSync/js/main/productionExecutable/kotlinZipline")
        val to = File("$basePath/../server/zipline-js/$ziplineJSVersion")
        //
        if (to.exists()) {
            to.deleteRecursively()
        }
        //
        to.mkdirs()
        //
        from.copyRecursively(to, overwrite = true)
    }
}
