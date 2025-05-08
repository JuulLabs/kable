package com.juul.kable.uniffi.plugin.tasks

import com.juul.kable.uniffi.plugin.UNIFFI_TASK_GROUP
import com.juul.kable.uniffi.plugin.UniffiKotlinExtensionAccessor
import com.juul.kable.uniffi.plugin.UniffiOs
import com.juul.kable.uniffi.plugin.UniffiTarget
import com.juul.kable.uniffi.plugin.cargoBuild
import com.juul.kable.uniffi.plugin.rustSources
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register

internal fun TaskContainer.registerCargoBuildTask(accessor: UniffiKotlinExtensionAccessor) {
    register("cargoBuild") {
        group = UNIFFI_TASK_GROUP

        for (target in UniffiTarget.all) {
            dependsOn("cargoBuild${target.taskName}")
        }
    }

    for (target in UniffiTarget.all) {
        registerCargoBuildTask(target, accessor)
    }
}

internal fun TaskContainer.registerCargoBuildTask(
    target: UniffiTarget,
    accessor: UniffiKotlinExtensionAccessor,
) {
    register<Exec>("cargoBuild${target.taskName}") {
        group = UNIFFI_TASK_GROUP

        onlyIf { target == UniffiTarget.current || accessor.enableCrossBuild }

        inputs.property("enableCrossBuild", accessor.enableCrossBuild)
        inputs.property("optimized", accessor.optimized)
        inputs.rustSources()
        outputs.cargoBuild(target, accessor.optimized)

        when (target.os) {
            UniffiOs.current -> commandLine("cargo", "build")
            UniffiOs.Linux -> commandLine("cross", "build")
            UniffiOs.Windows -> commandLine("cargo", "xwin", "build")
            UniffiOs.Apple -> error("Apple build task configured for non-apple platform")
        }

        args("--target", target.triple)
        if (accessor.optimized) {
            args("--release")
        }
    }
}
