plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.juul.kable.sample.gattserver"
    compileSdk = libs.versions.android.compile.get().toInt()

    defaultConfig {
        applicationId = "com.juul.kable.sample.gattserver"
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
}

dependencies {
    implementation(libs.compose.activity)
    implementation(libs.compose.material3)
    implementation(libs.kable.server)
    implementation(libs.lifecycle.viewmodel.compose)
}
