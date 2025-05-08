plugins {
    id("com.juul.kable.uniffi")
}

uniffiKotlin {
    enableCrossBuild = false // TODO: Fix for CI
    jvmToolchain = libs.versions.jvm.toolchain.get().toInt()
    optimized = false // TODO: Fix for CI
    packageName = "com.juul.kable.btleplug.ffi"
}
