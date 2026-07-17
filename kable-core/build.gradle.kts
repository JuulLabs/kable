plugins {
    alias(libs.plugins.android.bcv.bridge)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinter)
    id("kotlin-parcelize")
    kotlin("multiplatform")
}

fun isRunningOnMacOs() = System.getProperty("os.name").orEmpty().lowercase().startsWith("mac")

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    android {
        compileSdk = libs.versions.android.compile.get().toInt()
        minSdk = libs.versions.android.min.get().toInt()
        aarMetadata {
            minCompileSdk = libs.versions.android.min.get().toInt()
        }

        namespace = "com.juul.kable"
        withHostTest { }

        lint {
            abortOnError = true
            warningsAsErrors = true

            disable += "AndroidGradlePluginVersion"
            disable += "GradleDependency"

            // Calls to many functions on `BluetoothDevice`, `BluetoothGatt`, etc require `BLUETOOTH_CONNECT`
            // permission, which has been specified in the `AndroidManifest.xml`; rather than needing to annotate a
            // number of classes, we disable the "missing permission" lint check. Caution must be taken during later
            // Android version bumps to make sure we aren't missing any newly introduced permission requirements.
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
                optIn("com.juul.kable.ExperimentalKableApi")
                optIn("com.juul.kable.InternalKableApi")
                optIn("kotlin.concurrent.atomics.ExperimentalAtomicApi")
                optIn("kotlin.js.ExperimentalWasmJsInterop")
                optIn("kotlin.uuid.ExperimentalUuidApi")
                optIn("kotlinx.cinterop.UnsafeNumber")
            }
        }

        commonMain.dependencies {
            api(libs.kotlinx.coroutines)
            api(libs.kotlinx.io)
            implementation(libs.atomicfu)
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
            implementation(libs.kotlin.parcelize.runtime)
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
