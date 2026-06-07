plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
}

kotlin {
    android {
        compileSdk = libs.versions.android.compile.get().toInt()
        defaultConfig.minSdk = libs.versions.android.min.get().toInt()
        namespace = "com.juul.kable.permissions"
    }
}
