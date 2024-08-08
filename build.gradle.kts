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
    alias(libs.plugins.atomicfu) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.api)
}

tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.fileProvider(layout.buildDirectory.file("dokkaHtmlMultiModule").map { it.asFile })
}

allprojects {
    group = "com.juul.kable"

    repositories {
        google()
        mavenCentral()
    }

    listOf(
        org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile::class,
        org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon::class,
        org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile::class,
        org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile::class,
    ).forEach { kClass ->
        tasks.withType(kClass).configureEach {
            compilerOptions.suppressWarnings = (findProperty("suppressWarnings") as? String).toBoolean()
        }
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
