plugins {
    alias(libs.plugins.atomicfu)
    id("com.android.library")
    id("com.vanniktech.maven.publish")
    id("kotlin-parcelize")
    id("org.jetbrains.dokka")
    id("org.jmailen.kotlinter")
    kotlin("multiplatform")
}

kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    androidTarget().publishLibraryVariants("debug", "release")
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    // js().browser()
    // macosArm64()
    // macosX64()
    // jvm()

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.uuid.ExperimentalUuidApi")
            }
        }

        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.io)
            implementation(libs.datetime)
            implementation(libs.tuulbox.collections)
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

        androidUnitTest.dependencies {
            implementation(libs.equalsverifier)
            implementation(libs.mockk)
            implementation(libs.robolectric)
        }

        jsMain.dependencies {
            api(libs.wrappers.web)
            api(project.dependencies.platform(libs.wrappers.bom))
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

        disable += "AndroidGradlePluginVersion"
        disable += "GradleDependency"

        // Calls to many functions on `BluetoothDevice`, `BluetoothGatt`, etc require `BLUETOOTH_CONNECT` permission,
        // which has been specified in the `AndroidManifest.xml`; rather than needing to annotate a number of classes,
        // we disable the "missing permission" lint check. Caution must be taken during later Android version bumps to
        // make sure we aren't missing any newly introduced permission requirements.
        disable += "MissingPermission"
    }
}

dokka {
    pluginsConfiguration.html {
        footerMessage.set("(c) JUUL Labs, Inc.")
    }
}
