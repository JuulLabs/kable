plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jvm.get().toInt())

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    androidTarget()
    iosArm64()
    js().browser()
    macosX64()
    macosArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val mobileMain by creating {
            dependsOn(commonMain.get())
        }
        iosMain.get().dependsOn(mobileMain)
        androidMain.get().dependsOn(mobileMain)

        val nonMobileMain by creating {
            dependsOn(commonMain.get())
        }
        jsMain.get().dependsOn(nonMobileMain)
        macosMain.get().dependsOn(nonMobileMain)

        commonMain.dependencies {
            api(compose.runtime)
        }

        mobileMain.dependencies {
            api(libs.moko.permissions)
        }

        androidMain.dependencies {
            implementation(libs.compose.activity)
        }
    }
}

android {
    namespace = "com.juul.sensortag.permissions"
    compileSdk = libs.versions.android.compile.get().toInt()
    defaultConfig.minSdk = libs.versions.android.min.get().toInt()
    buildFeatures.compose = true
}
