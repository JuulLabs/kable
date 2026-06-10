/*
 * Copyright 2024 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())

    android {
        namespace = "dev.icerock.moko.permissions.bluetooth"
        compileSdk = libs.versions.android.compile.get().toInt()
        minSdk = libs.versions.android.min.get().toInt()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js().browser()
    jvm()
    macosX64()
    macosArm64()
    wasmJs().browser()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val nopMain by creating {
            dependsOn(commonMain.get())
        }
        macosMain.get().dependsOn(nopMain)
        jsMain.get().dependsOn(nopMain)
        jvmMain.get().dependsOn(nopMain)
        wasmJsMain.get().dependsOn(nopMain)

        commonMain.dependencies {
            api(projects.mokoPermissions)
            implementation(libs.coroutines)
        }
    }
}
