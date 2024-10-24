enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}

include(
    "app",
    "bluetooth",
    "ios",
    "permissions",
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
