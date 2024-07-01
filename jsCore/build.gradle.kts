plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

val jsCoreServerDir: String by rootProject.extra
val jsCoreFileName: String by rootProject.extra
val jsCoreVersion: Int by rootProject.extra

kotlin {
    js(IR) {
        browser {
            webpackTask {
                mainOutputFileName = jsCoreFileName
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.sharedJSCore)
        }
    }
}

tasks.register("deployJSCoreOnServer") {
    dependsOn("jsBrowserProductionWebpack")
    doLast {
        val basePath = project.projectDir.absolutePath
        val from = File("$basePath/build/kotlin-webpack/js/productionExecutable/$jsCoreFileName")
        val to = File("$basePath/../server/$jsCoreServerDir/$jsCoreVersion")
        //
        if (to.exists()) {
            to.deleteRecursively()
        }
        //
        to.mkdirs()
        //
        from.copyTo(File(to, jsCoreFileName), overwrite = true)
    }
}
