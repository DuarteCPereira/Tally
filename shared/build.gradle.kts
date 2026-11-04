plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.koinCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    androidLibrary {
        namespace = "com.tally.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            implementation(libs.koin.android)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(project(":dui-lib"))
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            api(libs.koin.core)
            api(libs.koin.annotations)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewmodel)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.koin.test)
        }
    }
}

sqldelight {
    databases {
        create("WorkoutDatabase") {
            packageName.set("com.tally.database")
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    add("iosArm64MainImplementation", libs.sqldelight.native.driver)
    add("iosSimulatorArm64MainImplementation", libs.sqldelight.native.driver)
}
