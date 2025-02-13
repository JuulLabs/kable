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
    "kable-log-engine-khronicle",
    "kable-server",
)
