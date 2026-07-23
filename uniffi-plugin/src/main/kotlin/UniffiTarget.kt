package com.juul.kable.uniffi.plugin

data class UniffiTarget(val arch: UniffiArch, val os: UniffiOs) {
    val triple: String get() = "${arch.tripleComponent}-${os.tripleComponent}"

    fun buildDirectory(optimized: Boolean): String =
        "target/$triple/${if (optimized) "release" else "debug"}"

    companion object {
        val current: UniffiTarget by lazy {
            UniffiTarget(UniffiArch.current, UniffiOs.current)
        }

        fun parse(triple: String): UniffiTarget {
            val arch = UniffiArch.entries.singleOrNull { triple.startsWith("${it.tripleComponent}-") }
                ?: error("Unsupported architecture in target triple: $triple")
            val os = UniffiOs.entries.singleOrNull { triple.endsWith("-${it.tripleComponent}") }
                ?: error("Unsupported OS in target triple: $triple")
            return UniffiTarget(arch, os)
        }
    }
}
