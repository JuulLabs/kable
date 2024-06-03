plugins {
    id("com.android.application")
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

kotlin {
    jvmToolchain(11)

    androidTarget()
    js {
        browser()
        binaries.executable()
    }
    macosX64 {
        binaries {
            executable {
                baseName = "sensortag"
                entryPoint = "com.juul.sensortag.main"
            }
        }
    }
    macosArm64 {
        binaries {
            executable {
                baseName = "sensortag"
                entryPoint = "com.juul.sensortag.main"
            }
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalStdlibApi")
        }

        commonMain.dependencies {
            api(libs.coroutines.core)
            implementation(libs.kable)
            implementation(libs.khronicle)
        }

        androidMain.dependencies {
            implementation(libs.bundles.accompanist)
            implementation(libs.bundles.compose)
            implementation(libs.bundles.krayon)
            implementation(libs.exercise.annotations)
        }
    }
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    compileSdk = libs.versions.android.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.min.get().toInt()
        targetSdk = libs.versions.android.target.get().toInt()
    }

    namespace = "com.juul.sensortag"

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    lint {
        abortOnError = false
    }

    packaging {
        resources.excludes.add("/META-INF/versions/*/previous-compilation-data.bin")
    }
}

dependencies {
    add("kspAndroid", libs.exercise.compile)
}
