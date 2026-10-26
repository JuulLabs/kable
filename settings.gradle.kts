rootProject.name = "kable"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("uniffi-plugin")
}

includeBuild("samples/sensortag")

include(
    "kable-btleplug-ffi",
    "kable-core",
    "kable-default-permissions",
    "kable-log-engine-khronicle",
)

// Building the Kotlin/Native Windows bindings requires a `x86_64-pc-windows-gnu` Rust toolchain,
// which is readily available on Windows hosts only (cross-compiling from macOS/Linux requires a
// mingw-w64 toolchain to be installed).
if (System.getProperty("os.name").orEmpty().lowercase().startsWith("windows")) {
    include("kable-btleplug-ffi-native")
}
