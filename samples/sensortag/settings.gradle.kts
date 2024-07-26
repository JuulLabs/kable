pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.android")
                useModule("com.android.tools.build:gradle:${requested.version}")
        }
    }
}

include("app")

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
