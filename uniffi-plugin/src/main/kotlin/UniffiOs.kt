package com.juul.kable.uniffi.plugin

enum class UniffiOs(
    val taskName: String,
    val jnaName: String,
    val tripleComponent: String,
    val library: Regex,
) {
    Apple("Macos", "darwin", "apple-darwin", """^lib.*\.dylib$""".toRegex()),
    Linux("Linux", "linux", "unknown-linux-gnu", """^lib.*\.so$""".toRegex()),
    Windows("Windows", "win32", "pc-windows-msvc", """^.*\.dll$""".toRegex()),
    ;

    companion object {
        val current: UniffiOs by lazy {
            val name = System.getProperty("os.name").lowercase()
            when {
                "mac" in name -> UniffiOs.Apple
                "windows" in name -> UniffiOs.Windows
                else -> UniffiOs.Linux
            }
        }
    }
}
