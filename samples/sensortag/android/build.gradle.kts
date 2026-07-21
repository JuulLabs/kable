plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())
}

android {
    namespace = "com.juul.sensortag.android"
    compileSdk = libs.versions.android.compile.get().toInt()

    defaultConfig {
        applicationId = "com.juul.sensortag.android"
        minSdk = 23
        targetSdk = libs.versions.android.target.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildFeatures.compose = true

    // Provides `java.time` for kotlinx.datetime on API < 26.
    // https://github.com/Kotlin/kotlinx-datetime?tab=readme-ov-file#using-in-your-projects
    compileOptions.isCoreLibraryDesugaringEnabled = true
}

dependencies {
    implementation(projects.shared)
    implementation(libs.compose.activity)
    coreLibraryDesugaring(libs.desugar)
}
