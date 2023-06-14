package com.juul.kable

import kotlinx.coroutines.flow.Flow

internal class BluezDBusBluetoothScanner : BluezBluetoothScanner {

    override val advertisements: Flow<BluezAdvertisement>
        get() = TODO("Not yet implemented")
}
