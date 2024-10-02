package com.juul.kable

import kotlinx.coroutines.CoroutineScope

@Deprecated(
    message = "Replaced with `Peripheral` builder function (not a CoroutineScope extension function).",
    replaceWith = ReplaceWith("Peripheral(advertisement, builderAction)"),
)
public expect fun CoroutineScope.peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction = {},
): Peripheral
