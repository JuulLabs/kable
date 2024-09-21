package com.juul.kable.logs

import com.juul.kable.toHexString
import kotlin.uuid.ExperimentalUuidApi

public val Hex: Logging.DataProcessor = Hex()

public class HexBuilder internal constructor() {

    /** Separator between each byte in the hex representation of data. */
    public var separator: String = " "

    /** Configures if hex representation of data should be lower-case. */
    public var lowerCase: Boolean = false
}

@OptIn(ExperimentalUuidApi::class)
public fun Hex(init: HexBuilder.() -> Unit = {}): Logging.DataProcessor {
    val config = HexBuilder().apply(init)
    return Logging.DataProcessor { data, _, _, _, _ ->
        data.toHexString(separator = config.separator, lowerCase = config.lowerCase)
    }
}
