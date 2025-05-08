package com.juul.kable.uniffi.plugin.tasks

import com.juul.kable.uniffi.plugin.UNIFFI_TASK_GROUP
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register

internal fun TaskContainer.registerCargoCleanTask() {
    named("clean") { dependsOn("cargoClean") }

    register<Exec>("cargoClean") {
        group = UNIFFI_TASK_GROUP
        outputs.upToDateWhen { !project.projectDir.resolve("target").exists() }
        commandLine("cargo", "clean")
    }
}
