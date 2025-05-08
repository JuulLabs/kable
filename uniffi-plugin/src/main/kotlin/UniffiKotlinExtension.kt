package com.juul.kable.uniffi.plugin

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Optional

interface UniffiKotlinExtension {
    /**
     * When true, builds all platforms. Otherwise, builds the local platform only.
     *
     * When running on a non-Apple platform, Apple builds will be disabled even if
     * this is true.
     */
    @get:Optional
    val enableCrossBuild: Property<Boolean>

    /** When not specified, defaults to 17. */
    @get:Optional
    val jvmToolchain: Property<Int>

    /**
     * When true, adds the `--release` tag to cargo builds.
     */
    @get:Optional
    val optimized: Property<Boolean>

    /** Package name to use for generated kotlin code. */
    val packageName: Property<String>
}

internal class UniffiKotlinExtensionAccessor(
    private val extension: UniffiKotlinExtension,
) {
    val enableCrossBuild: Boolean
        get() = extension.enableCrossBuild.getOrElse(false)

    val jvmToolchain: Int
        get() = extension.jvmToolchain.getOrElse(17)

    val optimized: Boolean
        get() = extension.optimized.getOrElse(false)

    val packageName: String
        get() = extension.packageName.get()
}
