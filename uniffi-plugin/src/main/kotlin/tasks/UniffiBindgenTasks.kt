package com.juul.kable.uniffi.plugin.tasks

import com.juul.kable.uniffi.plugin.UNIFFI_TASK_GROUP
import com.juul.kable.uniffi.plugin.UniffiKotlinExtensionAccessor
import com.juul.kable.uniffi.plugin.UniffiOs
import com.juul.kable.uniffi.plugin.UniffiTarget
import com.juul.kable.uniffi.plugin.cargoBuild
import com.juul.kable.uniffi.plugin.uniffiBindgenProject
import com.juul.kable.uniffi.plugin.uniffiOutputDirectory
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlTable
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register
import java.io.File

private const val CARGO_TOML_FMT = """
[package]
name = "uniffi-bindgen"
edition = "2024"

[dependencies]
uniffi = { version = "%s", features = ["bindgen", "cli", "tokio"] }
"""

private const val UNIFFI_TOML_FMT = """
[bindings.kotlin]
package_name = "%s"
"""

private const val MAIN_RS = """
fn main() {
    uniffi::uniffi_bindgen_main()
}
"""

internal fun TaskContainer.registerUniffiBindgenTasks(accessor: UniffiKotlinExtensionAccessor) {
    named("clean") { dependsOn("cleanUniffiBindgenCargoProject") }
    named("compileKotlin") { dependsOn("generateKotlinBindings") }

    // sourcesJar is created by the maven publishing plugin, so this plugin has to be applied after that one
    named("sourcesJar") { dependsOn("generateKotlinBindings") }

    register("cleanUniffiBindgenCargoProject") {
        group = UNIFFI_TASK_GROUP

        doLast { project.uniffiBindgenProject.deleteRecursively() }
    }

    register("generateUniffiBindgenCargoProject") {
        group = UNIFFI_TASK_GROUP

        val inputCargoToml = project.file("Cargo.toml")
        inputs.file(inputCargoToml)
        inputs.property("packageName", accessor.packageName)
        outputs.file(project.uniffiBindgenProject.resolve("Cargo.toml"))
        outputs.file(project.uniffiBindgenProject.resolve("uniffi.toml"))
        outputs.file(project.uniffiBindgenProject.resolve("src").resolve("main.rs"))

        doLast {
            project.uniffiBindgenProject
                .resolve("src")
                .mkdirs()
            project.uniffiBindgenProject
                .resolve("Cargo.toml")
                .writeText(CARGO_TOML_FMT.format(readUniffiVersion(inputCargoToml)).trimIndent())
            project.uniffiBindgenProject
                .resolve("uniffi.toml")
                .writeText(UNIFFI_TOML_FMT.format(accessor.packageName).trimIndent())
            project.uniffiBindgenProject
                .resolve("src")
                .resolve("main.rs")
                .writeText(MAIN_RS.trimIndent())
        }
    }

    register<Exec>("generateKotlinBindings") {
        group = UNIFFI_TASK_GROUP
        dependsOn("cargoBuild")
        dependsOn("generateUniffiBindgenCargoProject")

        inputs.property("optimized", accessor.optimized)

        inputs.cargoBuild(UniffiTarget.current, accessor.optimized)
        inputs.dir(project.uniffiBindgenProject)
        outputs.dir(project.uniffiOutputDirectory)

        workingDir(project.uniffiBindgenProject)
        commandLine("cargo", "run", "generate")
        args("--config", project.uniffiBindgenProject.resolve("uniffi.toml").absolutePath)
        args("--language", "kotlin")
        args("--out-dir", project.uniffiOutputDirectory.absolutePath)
        args("--no-format")
        doFirst {
            val directory = project.file(UniffiTarget.current.buildDirectory(accessor.optimized))
            val file = directory.list().orEmpty().single { it.matches(UniffiOs.current.library) }
            args("--library", directory.resolve(file).absolutePath)
        }
    }
}

private fun readUniffiVersion(cargoFile: File): String {
    val toml = Toml.parseToTomlTable(cargoFile.readText())
    val dependenciesTable = toml["dependencies"] as TomlTable
    return when (val uniffiEntry = dependenciesTable["uniffi"]) {
        is TomlTable -> (uniffiEntry["version"] as TomlLiteral).content
        is TomlLiteral -> uniffiEntry.content
        else -> error("Unexpected TOML entry: $uniffiEntry")
    }
}
