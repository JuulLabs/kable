buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.atomicfu) apply false
    alias(libs.plugins.validator)
}

tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(buildDir.resolve("dokkaHtmlMultiModule"))
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    tasks.withType<Test>().configureEach {
        testLogging {
            events("started", "passed", "skipped", "failed", "standardOut", "standardError")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showExceptions = true
            showStackTraces = true
            showCauses = true
        }
    }
}

// Workaround for https://youtrack.jetbrains.com/issue/KT-49109 that allows building on Apple Silicon
// This should no longer be needed with Kotlin version 1.6.20
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.0.0"
}
