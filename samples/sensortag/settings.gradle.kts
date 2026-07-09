enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}

plugins {
    // Provides repositories for auto-downloading JVM toolchains.
    // https://github.com/gradle/foojay-toolchains
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
