import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jmailen.kotlinter")
    `maven-publish`
}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += "-Xinline-classes"
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
            manifest.apply {
                srcFile("src/androidMain/AndroidManifest.xml")
            }
        }
    }
}
