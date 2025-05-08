package com.juul.kable.uniffi.plugin

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Optional

interface UniffiKotlinExtension {
    /** When true, adds the `--release` tag to cargo builds. */
    val optimized: Property<Boolean>

    /** Package name to use for generated kotlin code. */
    val packageName: Property<String>
}

internal class UniffiKotlinExtensionAccessor(
    private val extension: UniffiKotlinExtension,
) {
    val optimized: Boolean
        get() = extension.optimized.get()

    val packageName: String
        get() = extension.packageName.get()
}
