/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())

    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js().browser()
    jvm()
    macosX64()
    macosArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val nopMain by creating {
            dependsOn(commonMain.get())
        }
        macosMain.get().dependsOn(nopMain)
        jsMain.get().dependsOn(nopMain)
        jvmMain.get().dependsOn(nopMain)

        commonMain.dependencies {
            api(projects.mokoPermissions)
            api(compose.runtime)
        }

        androidMain.dependencies {
            implementation(libs.androidx.lifecycle)
            implementation(libs.compose.activity)
            implementation(libs.compose.ui)
        }
    }
}

android {
    namespace = "dev.icerock.moko.permissions.compose"
    compileSdk = libs.versions.android.compile.get().toInt()
    defaultConfig.minSdk = libs.versions.android.min.get().toInt()
    buildFeatures.compose = true
}
