package com.juul.kable.logs

public val Hex: Logging.DataProcessor = Hex()

public class HexBuilder internal constructor() {

    /** Separator between each byte in the hex representation of data. */
    public var separator: String = " "

    /** Configures if hex representation of data should be lower-case. */
    public var lowerCase: Boolean = false
}

public fun Hex(init: HexBuilder.() -> Unit = {}): Logging.DataProcessor {
    val config = HexBuilder().apply(init)
    val format = HexFormat {
        upperCase = !config.lowerCase
        bytes {
            byteSeparator = config.separator
        }
    }
    return Logging.DataProcessor { data, _, _, _, _ ->
        data.toHexString(format)
    }
}
