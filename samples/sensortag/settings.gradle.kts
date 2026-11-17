enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}

includeBuild("../..")

include(
    "android",
    "bluetooth",
    "ios",
    "moko-permissions",
    "moko-permissions-bluetooth",
    "moko-permissions-compose",
    "shared",
)
