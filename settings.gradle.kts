pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        jcenter()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.android")
                useModule("com.android.tools.build:gradle:${requested.version}")
            if (requested.id.id == "kotlinx-atomicfu")
                useModule("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${requested.version}")
        }
    }
}

include("core")
