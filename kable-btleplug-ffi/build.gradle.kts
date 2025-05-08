plugins {
    kotlin("jvm")
    id("com.juul.kable.uniffi")
}

kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    dependencies {
        api(libs.jna)
        api(libs.kotlinx.coroutines.core)
    }
}

uniffiKotlin {
    optimized = false // TODO: Fix for CI
    packageName = "com.juul.kable.btleplug.ffi"
}
