plugins {
    kotlin("multiplatform")
    id("kotlinx-atomicfu")
    id("com.android.library")
    id("org.jmailen.kotlinter")
    `maven-publish`
}

kotlin {
    explicitApi()

    js().browser()
    android()

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
            }
        }

        all {
            languageSettings.enableLanguageFeature("InlineClasses")
        }
    }
}

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(21)
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
