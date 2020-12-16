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
    id("org.jetbrains.dokka") version "1.4.10.2"
    id("kotlinx-atomicfu") version "0.14.4" apply false
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaMultiModuleTask>().configureEach {
    val dokkaDir = buildDir.resolve("dokkaHtmlMultiModule")
    outputDirectory.set(dokkaDir)
    doLast {
        dokkaDir.resolve("-modules.html").renameTo(dokkaDir.resolve("index.html"))
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}
