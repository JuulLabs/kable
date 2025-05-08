package com.juul.kable.uniffi.plugin.tasks

import com.juul.kable.uniffi.plugin.UNIFFI_TASK_GROUP
import com.juul.kable.uniffi.plugin.rustSources
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register

internal fun TaskContainer.registerCargoFormatTask() {
    register<Exec>("cargoFormat") {
        group = UNIFFI_TASK_GROUP
        inputs.rustSources()
        commandLine("cargo", "fmt")
    }
}
