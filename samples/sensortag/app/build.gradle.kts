plugins {
    id("com.android.application")
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

kotlin {
    android()
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

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.coroutines.core)
                implementation(libs.kable)
                implementation(libs.tuulbox.logging)
                implementation(libs.tuulbox.encoding)
                implementation(libs.tuulbox.coroutines)

                // todo: Remove once kotlinx.datetime 0.3.3+ is transitively pulled in.
                // This is needed because kotlinx.datetime 0.3.2 (which is not Kotlin 1.7.0 compatible) is transitively
                // pull in, so we explicitly upgrade to a version that is compatible with Kotlin 1.7.x.
                implementation(libs.datetime)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.bundles.compose)
                implementation(libs.bundles.accompanist)
                implementation(libs.exercise.annotations)
                implementation(libs.bundles.krayon)
            }
        }

        val nativeMain by creating {
            dependencies {
                implementation(libs.coroutines.macosx64)
                implementation(libs.stately)
            }
        }

        val macosX64Main by getting {
            dependsOn(nativeMain)
        }
    }
}

android {
    compileSdk = libs.versions.android.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.min.get().toInt()
        targetSdk = libs.versions.android.target.get().toInt()
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    lint {
        abortOnError = false
    }

    sourceSets {
        getByName("main").manifest.srcFile("src/androidMain/AndroidManifest.xml")
    }
}

dependencies {
    ksp(libs.exercise.compile)
}
