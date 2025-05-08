package com.juul.kable.uniffi.plugin

enum class UniffiOs(
    val jnaName: String,
    val tripleComponent: String,
    val library: Regex,
) {
    Apple("darwin", "apple-darwin", """^lib.*\.dylib$""".toRegex()),
    Linux("linux", "unknown-linux-gnu", """^lib.*\.so$""".toRegex()),
    Windows("win32", "pc-windows-msvc", """^.*\.dll$""".toRegex()),
    ;

    companion object {
        val current: UniffiOs by lazy {
            val name = System.getProperty("os.name").lowercase()
            when {
                "mac" in name -> Apple
                "windows" in name -> Windows
                else -> Linux
            }
        }
    }
}
