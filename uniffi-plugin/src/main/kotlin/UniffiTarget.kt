package com.juul.kable.uniffi.plugin

data class UniffiTarget(val arch: UniffiArch, val os: UniffiOs) {
    val taskName: String get() = "${arch.taskName}${os.taskName}"
    val triple: String get() = "${arch.tripleComponent}-${os.tripleComponent}"

    fun buildDirectory(optimized: Boolean): String =
        "target/$triple/${if (optimized) "release" else "debug"}"

    companion object {
        val current: UniffiTarget by lazy {
            UniffiTarget(UniffiArch.current, UniffiOs.current)
        }

        @OptIn(ExperimentalStdlibApi::class)
        val all: List<UniffiTarget> by lazy {
            UniffiArch.entries.flatMap { arch ->
                UniffiOs.entries.map { os ->
                    UniffiTarget(arch, os)
                }
            }.filter {
                // Pretend Apple doesn't exist on non-Apple hosts, life is better that way.
                it.os != UniffiOs.Apple || current.os == UniffiOs.Apple
            }
        }
    }
}
