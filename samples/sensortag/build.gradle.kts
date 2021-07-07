buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
    }
}

plugins {
    kotlin("multiplatform") version "1.5.20" apply false
    kotlin("kapt") version "1.5.20" apply false
    id("net.mbonnin.one.eight") version "0.2"
}

subprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }
}
