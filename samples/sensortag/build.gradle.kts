import java.net.URI

buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.0.2")
    }
}

plugins {
    kotlin("kapt") version "1.4.20" apply false
    id("net.mbonnin.one.eight") version "0.1"
}

subprojects {
    repositories {
        mavenLocal()
        google()
        jcenter()
        maven { url = URI("https://oss.sonatype.org/content/repositories/snapshots") }
    }
}
