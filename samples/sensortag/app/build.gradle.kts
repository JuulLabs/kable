plugins {
    id("com.android.application")
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

kotlin {
    android()
    js().browser()
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
                api(libs.kotlinx.coroutines.core)
                implementation(libs.kable)
                implementation(libs.tuulbox.logging)
                implementation(libs.tuulbox.encoding)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.material)
                implementation(libs.bundles.androidx)
                implementation(libs.exercise.annotations)
            }
        }

        val nativeMain by creating {
            dependencies {
                implementation(libs.kotlinx.coroutines.macosx64)
                implementation(libs.stately)
            }
        }

        val macosX64Main by getting {
            dependsOn(nativeMain)
        }
    }
}

android {
    compileSdkVersion(libs.versions.android.compile.get())

    defaultConfig {
        minSdkVersion(libs.versions.android.min.get())
    }

    buildFeatures {
        viewBinding = true
    }

    lintOptions {
        isAbortOnError = false
    }

    sourceSets {
        val main by getting {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }
}

dependencies {
    ksp(libs.exercise.compile)
}

// Fix failure when building JavaScript target (with Webpack 5).
// https://youtrack.jetbrains.com/issue/KT-48273
// todo: Remove once Kotlin is upgraded to 1.5.30.
afterEvaluate {
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
        versions.webpackDevServer.version = "4.0.0"
    }
}
