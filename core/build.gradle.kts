plugins {
    kotlin("multiplatform")
    `maven-publish`
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
                api("com.benasher44:uuid:0.2.2")
            }
        }
    }

    explicitApi()
}
