plugins {
    id("com.android.application")
    kotlin("multiplatform")
    kotlin("kapt")
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
                api(coroutines())
                implementation(kable())
                implementation(tuulbox("logging"))
                implementation(tuulbox("encoding"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(material())
                implementation(androidx.appcompat())
                implementation(androidx.recyclerview())
                implementation(androidx.lifecycle("viewmodel-ktx"))
                implementation(androidx.lifecycle("livedata-ktx"))
                implementation(androidx.activity("activity-ktx"))
                implementation(exercise("annotations"))
            }
        }

        val nativeMain by creating {
            dependencies {
                implementation(coroutines(module = "core-macosx64", version = "1.5.0-native-mt")) {
                    version {
                        strictly("1.5.0-native-mt")
                    }
                }
                implementation(stately("isolate"))
            }
        }

        val macosX64Main by getting {
            dependsOn(nativeMain)
        }
    }
}

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(21)
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
    "kapt"(exercise("compile"))
}

// Fix failure when building JavaScript target (with Webpack 5).
// https://youtrack.jetbrains.com/issue/KT-48273
// todo: Remove once Kotlin is upgraded to 1.5.30.
afterEvaluate {
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
        versions.webpackDevServer.version = "4.0.0"
    }
}
