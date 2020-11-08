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
                api(coroutines("core"))
                api(uuid())
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(coroutines("android"))
                implementation(atomicfu("jvm"))
            }
        }

        val macosX64Main by getting {
            kotlin.srcDir("src/appleMain/kotlin")

            dependencies {
                implementation(stately("isolate-macosx64"))
            }
        }

        val macosX64Test by getting {
            kotlin.srcDir("src/appleTest/kotlin")
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
