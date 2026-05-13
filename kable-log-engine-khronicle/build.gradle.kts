plugins {
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
    id("org.jmailen.kotlinter")
    kotlin("multiplatform")
}

fun isRunningOnMacOs() = System.getProperty("os.name").orEmpty().lowercase().startsWith("mac")

kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    // Build fails on Linux ARM64 host (when building Rust bindings for JAR distribution), so we
    // explicitly only include Native targets when running on MacOS.
    // https://youtrack.jetbrains.com/issue/KT-36871
    // https://youtrack.jetbrains.com/issue/KT-42445
    if (isRunningOnMacOs()) {
        iosArm64()
        iosSimulatorArm64()
        iosX64()
        macosArm64()
        macosX64()
    }

    js().browser()
    jvm()
    wasmJs().browser()

    sourceSets {
        commonMain.dependencies {
            api(project(":kable-core"))
            api(libs.khronicle)
        }
    }
}

dokka {
    pluginsConfiguration.html {
        footerMessage.set("(c) JUUL Labs, Inc.")
    }
}
