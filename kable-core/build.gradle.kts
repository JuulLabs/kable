plugins {
    // Android plugin must be before multiplatform plugin until https://youtrack.jetbrains.com/issue/KT-34038 is fixed.
    id("com.android.library")
    kotlin("multiplatform")
    id("kotlin-parcelize")
    alias(libs.plugins.atomicfu)
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
            api(libs.kotlinx.coroutines.core)
            api(libs.uuid)
            api(project(":kable-exceptions"))
            implementation(libs.datetime)
            implementation(libs.tuulbox.collections)
        }

        commonTest.dependencies {
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
