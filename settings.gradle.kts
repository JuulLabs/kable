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
    "kable-exceptions",
    "kable-log-engine-khronicle",
)
