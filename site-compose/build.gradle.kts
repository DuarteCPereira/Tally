import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

/**
 * The Tally website, written in Compose Multiplatform and compiled to WebAssembly.
 *
 * - Dev server: `./gradlew :site-compose:wasmJsBrowserDevelopmentRun`
 * - Static build: `./gradlew :site-compose:wasmJsBrowserDistribution`
 *   (output in `site-compose/build/dist/wasmJs/productionExecutable`)
 */
kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "tallySite"
        browser {
            commonWebpackConfig {
                outputFileName = "tallySite.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":dui-lib"))
            implementation(project(":site-shared"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "com.tally.site.compose.resources"
}
