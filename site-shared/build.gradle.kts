import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

// Becomes the npm package version.
version = "0.1.0"

/**
 * What both websites share: the page copy and the statistics preview logic.
 *
 * - `wasmJs` is consumed as a normal Gradle dependency by `:site-compose`.
 * - `js` is packed as an npm library (with TypeScript definitions) for `site-react`.
 *   Build it with `./gradlew :site-shared:jsBrowserProductionLibraryDistribution`.
 */
kotlin {
    explicitApi()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    js {
        outputModuleName = "tally-site-shared"
        browser()
        nodejs()
        useEsModules()
        binaries.library()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
