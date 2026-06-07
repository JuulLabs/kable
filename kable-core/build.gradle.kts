@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.atomicfu)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinter)
    id("kotlin-parcelize")
    kotlin("multiplatform")
}

fun isRunningOnMacOs() = System.getProperty("os.name").orEmpty().lowercase().startsWith("mac")

kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    android {
        compileSdk = libs.versions.android.compile.get().toInt()
        minSdk = libs.versions.android.min.get().toInt()
        namespace = "com.juul.kable"
        androidResources { enable = true }
        withHostTest {
            isIncludeAndroidResources = true
        }

        lint {
            abortOnError = true
            warningsAsErrors = true

            disable += "AndroidGradlePluginVersion"
            disable += "GradleDependency"

            // Calls to many functions on `BluetoothDevice`, `BluetoothGatt`, etc require
            // `BLUETOOTH_CONNECT` permission, which is specified in `AndroidManifest.xml`.
            disable += "MissingPermission"
        }
    }

    // Build fails on Linux ARM64 host (when building Rust bindings for JAR distribution), so we
    // explicitly only include Native targets when running on MacOS.
    // https://youtrack.jetbrains.com/issue/KT-36871
    // https://youtrack.jetbrains.com/issue/KT-42445
    if (isRunningOnMacOs()) {
        iosArm64()
        iosSimulatorArm64()
        iosX64()
        macosArm64()
        macosX64()
        watchosArm64()
        watchosSimulatorArm64()
        watchosDeviceArm64()
    }

    js().browser()
    jvm()
    wasmJs().browser()

    sourceSets {
        all {
            languageSettings {
                optIn("com.juul.kable.ExperimentalApi")
                optIn("kotlin.js.ExperimentalWasmJsInterop")
                optIn("kotlin.uuid.ExperimentalUuidApi")
                optIn("kotlinx.cinterop.UnsafeNumber")
            }
        }

        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.io)
        }

        commonTest.dependencies {
            implementation(kotlin("reflect")) // For `assertIs`.
            implementation(kotlin("test"))
            implementation(libs.khronicle)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            api(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.core)
            implementation(libs.androidx.startup)

            // Workaround for AtomicFU plugin not automatically adding JVM dependency for Android.
            // https://github.com/Kotlin/kotlinx-atomicfu/issues/145
            implementation(libs.atomicfu)

            implementation(libs.tuulbox.coroutines)
        }

        named("androidHostTest").dependencies {
            implementation(libs.equalsverifier)
            implementation(libs.mockk)
            implementation(libs.robolectric)
        }

        webMain.dependencies {
            api(libs.kotlinx.browser)
            api(libs.wrappers.browser)
            api(libs.wrappers.web)
            api(project.dependencies.platform(libs.wrappers.bom))
        }

        jvmMain.dependencies {
            implementation(project(":kable-btleplug-ffi"))
        }
    }
}

dokka {
    pluginsConfiguration.html {
        footerMessage.set("(c) JUUL Labs, Inc.")
    }
}
