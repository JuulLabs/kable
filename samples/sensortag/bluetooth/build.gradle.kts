plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jvm.get().toInt())

    androidTarget()
    iosArm64()
    js().browser()
    macosX64()
    macosArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.coroutines.core)
            api(projects.permissions)
        }

        androidMain.dependencies {
            implementation(libs.compose.activity)
            implementation(libs.tuulbox.coroutines)
        }
    }
}

android {
    namespace = "com.juul.sensortag.bluetooth"
    compileSdk = libs.versions.android.compile.get().toInt()
    defaultConfig.minSdk = libs.versions.android.min.get().toInt()
    buildFeatures.compose = true
}
