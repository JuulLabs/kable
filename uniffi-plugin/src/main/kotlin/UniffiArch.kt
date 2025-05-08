package com.juul.kable.uniffi.plugin

enum class UniffiArch(val jnaName: String, val tripleComponent: String) {
    X64("x86-64", "x86_64"),
    Arm64("aarch64", "aarch64"),
    ;

    companion object {
        val current: UniffiArch by lazy {
            val arch = System.getProperty("os.arch").lowercase()
            when {
                "arm" in arch || "aarch" in arch -> Arm64
                else -> X64
            }
        }
    }
}
