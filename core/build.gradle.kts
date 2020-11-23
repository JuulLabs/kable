plugins {
    // Android plugin must be before multiplatform plugin until https://youtrack.jetbrains.com/issue/KT-34038 is fixed.
    id("com.android.library")
    kotlin("multiplatform")
    id("kotlinx-atomicfu")
    id("org.jmailen.kotlinter")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

kotlin {
    explicitApi()

    js().browser()
    android {
        publishAllLibraryVariants()
    }
    macosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(coroutines("core", version = "1.3.9-native-mt-2"))
                api(uuid())
            }
        }

        val jsMain by getting {
            dependencies {
                api(coroutines("core"))
            }
        }

        val androidMain by getting {
            dependencies {
                api(coroutines("android"))
                implementation(atomicfu("jvm"))
            }
        }

        val macosX64Main by getting {
            dependencies {
                api(coroutines("core", version = "1.3.9-native-mt-2!!"))
                implementation(stately("isolate-macosx64"))
            }
        }

        all {
            languageSettings.enableLanguageFeature("InlineClasses")
        }
    }
}

atomicfu {
    transformJvm = true
    transformJs = false
}

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(21)
    }

    buildFeatures {
        buildConfig = false
    }

    lintOptions {
        isAbortOnError = true
        isWarningsAsErrors = true
    }

    sourceSets {
        val main by getting {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }
}
