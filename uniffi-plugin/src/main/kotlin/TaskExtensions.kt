package com.juul.kable.uniffi.plugin

import org.gradle.api.tasks.TaskInputs
import org.gradle.api.tasks.TaskOutputs

fun TaskInputs.cargoBuild(target: UniffiTarget, optimized: Boolean) {
    dir(target.buildDirectory(optimized))
}

fun TaskInputs.rustSources() {
    // Required
    dir("src")
    file("Cargo.toml")
    // Optional
    files(
        "Cargo.lock",
        "rust-toolchain.toml",
        "rustfmt.toml",
    )
}

fun TaskOutputs.cargoBuild(target: UniffiTarget, optimized: Boolean) {
    dir(target.buildDirectory(optimized))
}
