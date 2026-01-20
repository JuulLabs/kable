package com.juul.kable

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.io.IOException
import platform.Foundation.NSData

public interface CoreBluetoothPeripheral : Peripheral {

    @Throws(CancellationException::class, IOException::class)
    public suspend fun write(descriptor: Descriptor, data: NSData)

    @Throws(CancellationException::class, IOException::class)
    public suspend fun write(characteristic: Characteristic, data: NSData, writeType: WriteType)

    @Throws(CancellationException::class, IOException::class)
    public suspend fun readAsNSData(descriptor: Descriptor): NSData

    public fun observeAsNSData(
        characteristic: Characteristic,
        onSubscription: OnSubscriptionAction = {},
    ): Flow<NSData>

    @Throws(CancellationException::class, IOException::class)
    public suspend fun readAsNSData(characteristic: Characteristic): NSData
}
