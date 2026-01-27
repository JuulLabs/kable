plugins {
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
    id("org.jmailen.kotlinter")
    kotlin("multiplatform")
}

kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    iosArm64()
    iosX64()
    js().browser()
    macosArm64()
    macosX64()
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
