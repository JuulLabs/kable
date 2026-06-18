plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.serialization)
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    android {
        namespace = "com.juul.sensortag"
        compileSdk = libs.versions.android.compile.get().toInt()
        minSdk = libs.versions.android.min.get().toInt()
    }
    iosArm64 {
        binaries.framework {
            baseName = "ComposeApp"
            binaryOption("bundleId", "com.juul.sensortag.ios")
            binaryOption("bundleShortVersionString", "0.0.1")
            binaryOption("bundleVersion", "1")
            export(libs.kotlinx.coroutines)
        }
    }
    js {
        outputModuleName = "sample"
        browser {
            commonWebpackConfig {
                outputFileName = "sample.js"
            }
        }
        binaries.executable()
    }
    wasmJs {
        outputModuleName = "sample"
        browser {
            commonWebpackConfig {
                outputFileName = "sample.js"
            }
        }
        binaries.executable()
    }
    jvm()
    macosArm64 {
        binaries.executable {
            baseName = "sensortag"
            entryPoint = "com.juul.sensortag.main"
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        all {
            languageSettings.optIn("com.juul.kable.ExperimentalApi")
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        }

        val composeMain by creating {
            dependsOn(commonMain.get())
        }
        androidMain.get().dependsOn(composeMain)
        iosMain.get().dependsOn(composeMain)
        jsMain.get().dependsOn(composeMain)
        jvmMain.get().dependsOn(composeMain)
        wasmJsMain.get().dependsOn(composeMain)

        val notJsMain by creating {
            dependsOn(commonMain.get())
        }
        androidMain.get().dependsOn(notJsMain)
        appleMain.get().dependsOn(notJsMain)
        jvmMain.get().dependsOn(notJsMain)

        commonMain.dependencies {
            api(libs.kotlinx.coroutines)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.materialIconsExtended)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(libs.bundles.krayon)
            implementation(libs.bundles.voyager)
            implementation(libs.datetime)
            implementation(libs.kable)
            implementation(libs.khronicle)
            implementation(libs.serialization)
            implementation(projects.bluetooth)
        }

        androidMain.dependencies {
            implementation(libs.compose.activity)
            implementation(libs.kable.permissions)
        }

        composeMain.dependencies {
            implementation(libs.krayon.compose)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }

        notJsMain.dependencies {
            implementation(libs.androidx.lifecycle)
        }
    }
}

compose {
    desktop {
        application {
            mainClass = "com.juul.sensortag.MainKt"
        }
    }
}
