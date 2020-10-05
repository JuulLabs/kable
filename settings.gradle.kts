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
        }
    }
}

include("core")
