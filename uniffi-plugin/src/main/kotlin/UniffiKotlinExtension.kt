package com.juul.kable.uniffi.plugin

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Optional

interface UniffiKotlinExtension {
    /**
     * Command used to build the dynamic library, for example `cross`
     * (https://github.com/cross-rs/cross). Defaults to `cargo`.
     */
    @get:Optional
    val cargoCommand: Property<String>

    /** When true, adds the `--release` tag to cargo builds. */
    val optimized: Property<Boolean>

    /** Package name to use for generated kotlin code. */
    val packageName: Property<String>

    /**
     * Rust target triple (e.g. `aarch64-unknown-linux-gnu`) to build the dynamic library for.
     * Defaults to the host's target.
     */
    @get:Optional
    val target: Property<String>
}

internal class UniffiKotlinExtensionAccessor(
    private val extension: UniffiKotlinExtension,
) {
    val cargoCommand: String
        get() = extension.cargoCommand.getOrElse("cargo")

    val optimized: Boolean
        get() = extension.optimized.get()

    val packageName: String
        get() = extension.packageName.get()

    val target: UniffiTarget
        get() = extension.target.orNull?.let(UniffiTarget::parse) ?: UniffiTarget.current
}
