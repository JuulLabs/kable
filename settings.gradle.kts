rootProject.name = "kable"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("../uniffi-kotlin-gradle-plugin/plugin/")
}

include(
    "kable-btleplug-ffi",
    "kable-core",
    "kable-default-permissions",
    "kable-log-engine-khronicle",
)
