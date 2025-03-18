rootProject.name = "kable"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

include(
    "kable-core",
    "kable-default-permissions",
    "kable-log-engine-khronicle",
)
