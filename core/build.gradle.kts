plugins {
    kotlin("multiplatform")
    id("org.jmailen.kotlinter")
    `maven-publish`
}

kotlin {
    explicitApi()

    js().browser()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(coroutines("core"))
                api(uuid())
            }
        }
    }
}
