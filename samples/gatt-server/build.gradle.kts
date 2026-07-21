buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
}

allprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}
