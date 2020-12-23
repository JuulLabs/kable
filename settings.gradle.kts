pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        jcenter()
    }

    resolutionStrategy {
        eachPlugin {
            when {
                requested.id.id == "binary-compatibility-validator" ->
                    useModule("org.jetbrains.kotlinx:binary-compatibility-validator:${requested.version}")

                requested.id.namespace == "com.android" ->
                    useModule("com.android.tools.build:gradle:${requested.version}")

                requested.id.id == "kotlinx-atomicfu" ->
                    useModule("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${requested.version}")
            }
        }
    }
}

include("core")
