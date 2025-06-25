plugins {
    id("com.android.library")
    id("com.vanniktech.maven.publish")
    kotlin("android")
}

kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())
}

android {
    compileSdk = libs.versions.android.compile.get().toInt()
    defaultConfig.minSdk = libs.versions.android.min.get().toInt()
    namespace = "com.juul.kable"
}
