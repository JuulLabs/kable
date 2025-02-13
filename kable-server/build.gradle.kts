plugins {
    // Android plugin must be before multiplatform plugin until https://youtrack.jetbrains.com/issue/KT-34038 is fixed.
    id("com.android.library")
    kotlin("multiplatform")
    alias(libs.plugins.atomicfu)
    id("org.jmailen.kotlinter")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    androidTarget().publishAllLibraryVariants()
    macosArm64()
    macosX64()

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.uuid.ExperimentalUuidApi")
            }
        }

        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.io)
            api(projects.kableCore)
            implementation(libs.datetime)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            api(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.core)
            implementation(libs.androidx.startup)

            // Workaround for AtomicFU plugin not automatically adding JVM dependency for Android.
            // https://github.com/Kotlin/kotlinx-atomicfu/issues/145
            implementation(libs.atomicfu)
        }

        androidUnitTest.dependencies {
            implementation(libs.equalsverifier)
            implementation(libs.mockk)
        }
    }
}

android {
    compileSdk = libs.versions.android.compile.get().toInt()
    defaultConfig.minSdk = libs.versions.android.min.get().toInt()

    namespace = "com.juul.kable.server"

    lint {
        abortOnError = true
        warningsAsErrors = true

        disable += "AndroidGradlePluginVersion"
        disable += "GradleDependency"
    }
}
