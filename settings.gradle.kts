pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    resolutionStrategy {
        eachPlugin {
            when {
                requested.id.id == "binary-compatibility-validator" ->
                    useModule("org.jetbrains.kotlinx:binary-compatibility-validator:${requested.version}")

                requested.id.namespace == "com.android" ->
                    useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
}

include(
    "exceptions",
    "core",
    "log-engine-khronicle",
    "log-engine-tuulbox",
)
