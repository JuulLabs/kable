package com.juul.kable

import com.juul.kable.external.RequestDeviceOptions
import js.objects.unsafeJso
import kotlin.uuid.Uuid

/** https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth/requestDevice */
public fun Options(builder: OptionsBuilder.() -> Unit): Options =
    OptionsBuilder().apply(builder).build()

public data class Options internal constructor(
    internal val filters: List<FilterPredicate>,
    internal val optionalServices: List<Uuid>,
)

internal fun Options.toRequestDeviceOptions(): RequestDeviceOptions {
    val jsFilters = filters.toBluetoothLEScanFilterInit()
    val jsOptionalServices = optionalServices.toBluetoothServiceUUID()

    return unsafeJso {
        if (jsFilters.isEmpty()) {
            acceptAllDevices = true
        } else {
            filters = jsFilters
        }

        if (jsOptionalServices.isNotEmpty()) {
            optionalServices = jsOptionalServices
        }
    }
}
