plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jvm.get().toInt())

    android {
        namespace = "com.juul.sensortag.bluetooth"
        compileSdk = libs.versions.android.compile.get().toInt()
        minSdk = libs.versions.android.min.get().toInt()
    }
    iosArm64()
    js().browser()
    jvm()
    macosX64()
    macosArm64()
    wasmJs().browser()

    sourceSets {
        commonMain.dependencies {
            api(libs.coroutines)
            api(projects.mokoPermissionsBluetooth)
            api(projects.mokoPermissionsCompose)
        }

        androidMain.dependencies {
            implementation(libs.compose.activity)
            implementation(libs.tuulbox.coroutines)
        }
    }
}
