plugins {
    // Android plugin must be before multiplatform plugin until https://youtrack.jetbrains.com/issue/KT-34038 is fixed.
    id("com.android.library")
    kotlin("multiplatform")
    id("kotlin-parcelize")
    id("kotlinx-atomicfu")
    id("org.jmailen.kotlinter")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    androidTarget().publishAllLibraryVariants()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    js().browser()
    macosArm64()
    macosX64()
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(project(":exceptions"))
            api(libs.kotlinx.coroutines.core)
            api(libs.uuid)
            implementation(libs.tuulbox.collections)

        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.tuulbox.logging)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            api(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.core)
            implementation(libs.androidx.startup)
            implementation(libs.tuulbox.coroutines)
        }
    }
}

android {
    compileSdk = libs.versions.android.compile.get().toInt()
    defaultConfig.minSdk = libs.versions.android.min.get().toInt()

    namespace = "com.juul.kable"

    lint {
        abortOnError = true
        warningsAsErrors = true

        // Calls to many functions on `BluetoothDevice`, `BluetoothGatt`, etc require `BLUETOOTH_CONNECT` permission,
        // which has been specified in the `AndroidManifest.xml`; rather than needing to annotate a number of classes,
        // we disable the "missing permission" lint check. Caution must be taken during later Android version bumps to
        // make sure we aren't missing any newly introduced permission requirements.
        disable += "MissingPermission"

        disable += "GradleDependency"
    }
}
