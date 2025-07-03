package com.juul.kable.uniffi.plugin.tasks

import com.juul.kable.uniffi.plugin.UNIFFI_TASK_GROUP
import com.juul.kable.uniffi.plugin.rustSources
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register

internal fun TaskContainer.registerCargoTestTask() {
    named("test") { dependsOn("cargoTest") }

    register<Exec>("cargoTest") {
        group = UNIFFI_TASK_GROUP
        inputs.rustSources()
        commandLine("cargo", "test")
    }
}
