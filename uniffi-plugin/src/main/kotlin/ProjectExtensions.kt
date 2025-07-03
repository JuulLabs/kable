package com.juul.kable.uniffi.plugin

import org.gradle.api.Project
import java.io.File

internal val Project.uniffiOutputDirectory: File
    get() = this.layout.buildDirectory.asFile.get()
        .resolve("generated")
        .resolve("uniffiKotlin")
        .resolve(KOTLIN_SOURCE_SET)
        .resolve("kotlin")

internal val Project.uniffiBindgenProject: File
    get() = this.layout.buildDirectory.asFile.get()
        .resolve("generated")
        .resolve("uniffiKotlinBindgen")
