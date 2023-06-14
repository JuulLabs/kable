package com.juul.kable

import kotlinx.coroutines.flow.Flow

public interface BluezBluetoothScanner : Scanner {
    public override val advertisements: Flow<BluezAdvertisement>
}
