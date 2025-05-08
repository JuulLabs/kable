package com.juul.kable.uniffi.plugin

enum class UniffiArch(val taskName: String, val jnaName: String, val tripleComponent: String) {
    X64("X64", "x86-64", "x86_64"),
    Arm64("Arm64", "aarch64", "aarch64"),
    ;

    companion object {
        val current: UniffiArch by lazy {
            val arch = System.getProperty("os.arch").lowercase()
            when {
                "arm" in arch || "aarch" in arch -> UniffiArch.Arm64
                else -> UniffiArch.X64
            }
        }
    }
}
