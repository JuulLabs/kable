plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.multiplatform)
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    android {
        namespace = "com.juul.sensortag.bluetooth"
        compileSdk = libs.versions.android.compile.get().toInt()
        minSdk = libs.versions.android.min.get().toInt()
    }
    iosArm64()
    js().browser()
    jvm()
    macosArm64()
    wasmJs().browser()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines)
            api(projects.mokoPermissionsBluetooth)
            api(projects.mokoPermissionsCompose)
        }

        androidMain.dependencies {
            implementation(libs.compose.activity)
        }
    }
}
