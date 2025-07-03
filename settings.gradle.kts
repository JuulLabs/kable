rootProject.name = "kable"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("./uniffi-plugin/")
}

include(
    "kable-btleplug-ffi",
    "kable-core",
    "kable-default-permissions",
    "kable-log-engine-khronicle",
)
