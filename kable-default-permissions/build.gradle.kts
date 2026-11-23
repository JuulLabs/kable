plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
}

kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())
}

android {
    compileSdk {
        version = release(libs.versions.android.compile.major.get().toInt()) {
            minorApiLevel = libs.versions.android.compile.minor.get().toInt()
        }
    }
    defaultConfig {
        minSdk = libs.versions.android.min.get().toInt()
        aarMetadata.minCompileSdk = libs.versions.android.min.get().toInt()
    }
    namespace = "com.juul.kable.permissions"
}
