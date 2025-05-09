package com.juul.kable

import com.juul.kable.btleplug.BtleplugAdvertisement
import com.juul.kable.btleplug.BtleplugPeripheral

public actual fun Peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral {
    check(advertisement is BtleplugAdvertisement) {
        "Unexpected advertisement type: ${advertisement::class}."
    }
    return Peripheral(advertisement.identifier, builderAction)
}

public fun Peripheral(
    identifier: Identifier,
    builderAction: PeripheralBuilderAction = {},
): Peripheral {
    val builder = PeripheralBuilder().apply(builderAction)
    return BtleplugPeripheral(
        identifier,
        builder.logging,
    )
}
