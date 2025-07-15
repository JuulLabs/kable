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

include(
    "app",
    "bluetooth",
    "ios",
    "moko-permissions",
    "moko-permissions-bluetooth",
    "moko-permissions-compose",
)

// Configure sibling composite projects (`../<project>`) by adding `composite.<project>=true` to `local.properties`.
java.util.Properties()
    .apply {
        rootProject.projectDir
            .resolve("local.properties")
            .normalize()
            .takeIf(File::exists)
            ?.let { java.io.FileInputStream(it) }
            ?.use(::load)
    }
    .run {
        stringPropertyNames()
            .filter { it.startsWith("composite.") && getProperty(it).toBoolean() }
            .map { it.substringAfter('.') }
            .onEach { logger.lifecycle("Including '$it' as a composite build") }
            .map { "../$it" }
            .forEach(::includeBuild)
    }
