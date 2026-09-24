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

    // CI builds Linux dynamic libraries with `cross` (https://github.com/cross-rs/cross) so that
    // they link against an older glibc than what is available on the GitHub runners, for
    // compatibility with older Linux distributions (e.g. Raspberry Pi OS).
    // https://github.com/JuulLabs/kable/issues/990
    System.getenv("UNIFFI_CARGO")?.takeIf(String::isNotBlank)?.let { cargoCommand = it }
    System.getenv("UNIFFI_TARGET")?.takeIf(String::isNotBlank)?.let { target = it }
}
