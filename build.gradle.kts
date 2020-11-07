buildscript {
    repositories {
        google()
        jcenter()
    }
}

plugins {
    kotlin("multiplatform") version "1.4.10" apply false
    id("com.android.library") version "4.0.2" apply false
    id("org.jmailen.kotlinter") version "3.2.0" apply false
    id("com.vanniktech.maven.publish") version "0.13.0" apply false
    id("org.jetbrains.dokka") version "1.4.10.2" apply false
    id("kotlinx-atomicfu") version "0.14.4" apply false
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}
