package com.juul.kable.uniffi.plugin.tasks

import com.juul.kable.uniffi.plugin.KOTLIN_SOURCE_SET
import com.juul.kable.uniffi.plugin.UniffiKotlinExtensionAccessor
import com.juul.kable.uniffi.plugin.UniffiTarget
import com.juul.kable.uniffi.plugin.cargoBuild
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register

internal fun TaskContainer.registerCopyDynamicLibraryResourcesTask(accessor: UniffiKotlinExtensionAccessor) {
    named("processResources") { dependsOn("copyDynamicLibraryResources") }

    register<Copy>("copyDynamicLibraryResources") {
        dependsOn("cargoBuild")

        inputs.property("enableCrossBuild", accessor.enableCrossBuild)
        inputs.property("optimized", accessor.optimized)
        destinationDir = project.layout.buildDirectory.get().asFile
            .resolve("resources")
            .resolve(KOTLIN_SOURCE_SET)

        val targets = if (accessor.enableCrossBuild) UniffiTarget.all else listOf(UniffiTarget.current)
        for (target in targets) {
            inputs.cargoBuild(target, accessor.optimized)
            from(target.buildDirectory(accessor.optimized)) {
                include { it.name.matches(target.os.library) }
                into("${target.os.jnaName}-${target.arch.jnaName}")
            }
        }
    }
}
