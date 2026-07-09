plugins {
    kotlin("jvm")
    alias(libs.plugins.maven.publish)
    id("com.juul.kable.uniffi")
}

kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    dependencies {
        api(libs.jna)
        api(libs.kotlinx.coroutines)
    }
}

uniffiKotlin {
    optimized = System.getenv("CI").toBoolean()
    packageName = "com.juul.kable.btleplug.ffi"
}
