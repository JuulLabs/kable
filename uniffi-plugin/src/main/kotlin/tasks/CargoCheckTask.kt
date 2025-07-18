package com.juul.kable.uniffi.plugin.tasks

import com.juul.kable.uniffi.plugin.UNIFFI_TASK_GROUP
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register

internal fun TaskContainer.registerCargoCheckTask() {
    register<Exec>("cargoCheck") {
        group = UNIFFI_TASK_GROUP

        commandLine("cargo", "check")
    }
}
