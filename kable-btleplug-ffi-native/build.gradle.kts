import gobley.gradle.rust.targets.RustPosixTarget

plugins {
    alias(libs.plugins.maven.publish)
    kotlin("multiplatform")
    alias(libs.plugins.kotlin.atomicfu)
    alias(libs.plugins.gobley.cargo)
    alias(libs.plugins.gobley.uniffi)
}

fun isRunningOnWindows() = System.getProperty("os.name").orEmpty().lowercase().startsWith("windows")

kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    if (isRunningOnWindows()) {
        mingwX64()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

cargo {
    packageDirectory = rootProject.layout.projectDirectory.dir("kable-btleplug-ffi")
}

uniffi {
    generateFromLibrary {
        packageName = "com.juul.kable.btleplug.ffi"
        build = RustPosixTarget.MinGWX64
    }
}
