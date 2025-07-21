plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
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
    optimized = System.getenv("CI").toBoolean()
    packageName = "com.juul.kable.btleplug.ffi"
}
