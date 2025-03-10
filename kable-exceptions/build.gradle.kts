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
    iosSimulatorArm64()
    iosX64()
    js().browser()
    jvm()
    macosArm64()
    macosX64()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.io)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
