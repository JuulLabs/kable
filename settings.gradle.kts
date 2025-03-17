rootProject.name = "kable"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
