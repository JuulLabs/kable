package com.juul.kable

import kotlinx.coroutines.flow.Flow
import org.khronos.webgl.DataView

public interface WebBluetoothPeripheral : Peripheral {
    public suspend fun readAsDataView(characteristic: Characteristic): DataView
    public suspend fun readAsDataView(descriptor: Descriptor): DataView
    public fun observeDataView(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction = {},
    ): Flow<DataView>
}
