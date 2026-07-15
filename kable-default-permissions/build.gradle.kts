plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
}

android {
    compileSdk = libs.versions.android.compile.get().toInt()
    defaultConfig.minSdk = libs.versions.android.min.get().toInt()
    namespace = "com.juul.kable.permissions"

    // AGP 9 defaults `minCompileSdk` to this library's `compileSdk`, which forces consumers
    // onto the same (or newer) compile SDK. Relax it to Kable's minimum supported Android
    // version. https://github.com/JuulLabs/kable/issues/1206
    defaultConfig.aarMetadata.minCompileSdk = libs.versions.android.min.get().toInt()
}
