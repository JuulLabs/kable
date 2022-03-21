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

// Workaround for:
// java.lang.NoSuchMethodError: No static method setContent$default(..)
// https://youtrack.jetbrains.com/issue/KT-38694
// https://github.com/avdim/compose_mpp_workaround
configurations {
    create("composeCompiler") {
        isCanBeConsumed = false
    }
}
dependencies {
    add("composeCompiler", libs.compose.compiler.get())
}
android {
    afterEvaluate {
        val composeCompilerJar =
            configurations["composeCompiler"]
                .resolve()
                .singleOrNull()
                ?: error("Please add `androidx.compose.compiler:compiler` as the only `composeCompiler` dependency.")
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions.freeCompilerArgs += listOf("-Xuse-ir", "-Xplugin=$composeCompilerJar")
        }
    }
}
// End workaround
