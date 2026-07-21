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
    "bluetooth-sig-assigned-numbers",
    "kable-btleplug-ffi",
    "kable-core",
    "kable-default-permissions",
    "kable-log-engine-khronicle",
)
