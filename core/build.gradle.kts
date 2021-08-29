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

    android {
        publishAllLibraryVariants()
    }
    js().browser()
    iosX64()
    iosArm32()
    iosArm64()
    macosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(coroutines())
                api(uuid())
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(wrappers())
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        val androidMain by getting {
            dependencies {
                api(coroutines("android"))
                implementation(atomicfu("jvm"))
                implementation(androidx.startup())
            }
        }

        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        val macosX64Main by getting {
            dependencies {
                implementation(stately("isolate-macosx64"))
            }
        }

        val iosX64Main by getting {
            dependencies {
                implementation(stately("isolate-iosx64"))
            }
        }

        val iosArm32Main by getting {
            dependencies {
                implementation(stately("isolate-iosarm32"))
            }
        }

        val iosArm64Main by getting {
            dependencies {
                implementation(stately("isolate-iosarm64"))
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
