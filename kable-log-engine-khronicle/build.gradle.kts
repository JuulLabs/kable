plugins {
    kotlin("multiplatform")
    id("org.jmailen.kotlinter")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}



publishing {
    repositories {
        maven {
            name = "LocalRepo"
            url = uri(project.rootDir.resolve("repo").absolutePath)
        }
    }
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

    sourceSets {
        commonMain.dependencies {
            api(project(":kable-core"))
            api(libs.khronicle)
        }
    }
}
