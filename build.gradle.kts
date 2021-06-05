buildscript {
    repositories {
        google()
        jcenter()
    }
}

plugins {
    kotlin("multiplatform") version "1.5.10" apply false
    id("com.android.library") version "4.1.3" apply false
    id("org.jmailen.kotlinter") version "3.4.4" apply false
    id("com.vanniktech.maven.publish") version "0.15.1" apply false
    id("org.jetbrains.dokka") version "1.4.32"
    id("kotlinx-atomicfu") version "0.16.1" apply false
    id("binary-compatibility-validator") version "0.5.0"
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
