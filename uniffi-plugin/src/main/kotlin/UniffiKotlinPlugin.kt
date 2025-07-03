package com.juul.kable.uniffi.plugin

import com.juul.kable.uniffi.plugin.tasks.registerCargoBuildTask
import com.juul.kable.uniffi.plugin.tasks.registerCargoCheckTask
import com.juul.kable.uniffi.plugin.tasks.registerCargoCleanTask
import com.juul.kable.uniffi.plugin.tasks.registerCargoFormatTask
import com.juul.kable.uniffi.plugin.tasks.registerCargoLintTasks
import com.juul.kable.uniffi.plugin.tasks.registerCargoTestTask
import com.juul.kable.uniffi.plugin.tasks.registerCopyDynamicLibraryResourcesTask
import com.juul.kable.uniffi.plugin.tasks.registerUniffiBindgenTasks
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer

abstract class UniffiKotlinPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply("org.jetbrains.kotlin.jvm")

        project.extensions.create("uniffiKotlin", UniffiKotlinExtension::class.java)

        project.afterEvaluate {
            val extension = extensions.getByType(UniffiKotlinExtension::class.java)
            val accessor = UniffiKotlinExtensionAccessor(extension)
            configureKotlinJvm()
            tasks.registerCargoBuildTask(accessor)
            tasks.registerCargoCheckTask()
            tasks.registerCargoCleanTask()
            tasks.registerCargoFormatTask()
            tasks.registerCargoLintTasks()
            tasks.registerCargoTestTask()
            tasks.registerCopyDynamicLibraryResourcesTask(accessor)
            tasks.registerUniffiBindgenTasks(accessor)
        }
    }
}

private fun Project.configureKotlinJvm() {
    val sourceSet = extensions
        .getByType(KotlinSourceSetContainer::class.java)
        .sourceSets
        .getByName(KOTLIN_SOURCE_SET)

    sourceSet.kotlin.srcDir(uniffiOutputDirectory)
}
