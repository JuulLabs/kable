package com.juul.kable

import kotlinx.coroutines.flow.Flow

public interface WebBluetoothScanner : Scanner {
    public override val advertisements: Flow<WebBluetoothAdvertisement>
}
