package com.juul.kable

import kotlinx.coroutines.flow.Flow
import platform.Foundation.NSData
import kotlin.coroutines.cancellation.CancellationException

public interface CoreBluetoothPeripheral : Peripheral {

    @Throws(CancellationException::class, IOException::class)
    public suspend fun write(descriptor: Descriptor, data: NSData)

    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun write(characteristic: Characteristic, data: NSData, writeType: WriteType)

    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun readAsNSData(descriptor: Descriptor): NSData

    public fun observeAsNSData(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction = {},
    ): Flow<NSData>

    @Throws(CancellationException::class, IOException::class, NotReadyException::class)
    public suspend fun readAsNSData(characteristic: Characteristic): NSData
}
