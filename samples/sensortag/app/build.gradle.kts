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
                implementation(kable(version = "main-SNAPSHOT"))
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

        val macosX64Main by getting {
            dependencies {
                implementation(stately("isolate-macosx64"))
            }
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
            res.srcDirs("src/androidMain/resources")
        }
    }
}

dependencies {
    "kapt"(exercise("compile"))
}
