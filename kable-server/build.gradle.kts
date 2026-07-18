plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinter)
    kotlin("multiplatform")
}

fun isRunningOnMacOs() = System.getProperty("os.name").orEmpty().lowercase().startsWith("mac")

kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    android {
        compileSdk = libs.versions.android.compile.get().toInt()
        minSdk = libs.versions.android.min.get().toInt()
        aarMetadata {
            minCompileSdk = libs.versions.android.min.get().toInt()
        }

        namespace = "com.juul.kable.server"
        withHostTest { }

        lint {
            abortOnError = true
            warningsAsErrors = true

            disable += "AndroidGradlePluginVersion"
            disable += "GradleDependency"

            // Calls to many functions on `BluetoothGattServer`, `BluetoothLeAdvertiser`, etc.
            // require `BLUETOOTH_CONNECT` and/or `BLUETOOTH_ADVERTISE` permissions, which must be
            // declared (and requested at runtime) by the consuming application; rather than needing
            // to annotate a number of classes, we disable the "missing permission" lint check.
            disable += "MissingPermission"
        }
    }

    // Peripheral role is not supported on JS (Web Bluetooth is client-only) nor JVM (btleplug is
    // central-only), so (unlike `kable-core`) neither target is configured for `kable-server`.
    //
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

    sourceSets {
        all {
            languageSettings {
                optIn("com.juul.kable.ExperimentalKableApi")
                optIn("kotlin.concurrent.atomics.ExperimentalAtomicApi")
                optIn("kotlin.uuid.ExperimentalUuidApi")
                optIn("kotlinx.cinterop.UnsafeNumber")
            }
        }

        commonMain.dependencies {
            api(project(":kable-core"))
            api(libs.kotlinx.coroutines)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            api(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.core)
            implementation(libs.androidx.startup)
        }

        named("androidHostTest").dependencies {
            implementation(libs.mockk)
            implementation(libs.robolectric)
        }
    }
}

dokka {
    pluginsConfiguration.html {
        footerMessage.set("(c) JUUL Labs, Inc.")
    }
}
