package com.juul.kable.uniffi.plugin.tasks

import com.juul.kable.uniffi.plugin.UNIFFI_TASK_GROUP
import com.juul.kable.uniffi.plugin.UniffiKotlinExtensionAccessor
import com.juul.kable.uniffi.plugin.cargoBuild
import com.juul.kable.uniffi.plugin.rustSources
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register

internal fun TaskContainer.registerCargoBuildTask(accessor: UniffiKotlinExtensionAccessor) {
    register<Exec>("cargoBuild") {
        group = UNIFFI_TASK_GROUP

        inputs.property("cargoCommand", accessor.cargoCommand)
        inputs.property("optimized", accessor.optimized)
        inputs.property("target", accessor.target.triple)
        inputs.rustSources()
        outputs.cargoBuild(accessor.target, accessor.optimized)

        commandLine(accessor.cargoCommand, "build")
        args("--target", accessor.target.triple)
        if (accessor.optimized) {
            args("--release")
        }
    }
}
