plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())

    android {
        namespace = "dev.icerock.moko.permissions"
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
            implementation(libs.coroutines)
        }

        androidMain.dependencies {
            implementation(libs.compose.activity)
            implementation(libs.androidx.lifecycle)
        }
    }
}
