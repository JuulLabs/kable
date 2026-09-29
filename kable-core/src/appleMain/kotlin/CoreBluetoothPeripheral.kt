package com.juul.kable

import kotlinx.coroutines.flow.Flow
import kotlinx.io.IOException
import platform.CoreBluetooth.CBPeripheral
import platform.Foundation.NSData
import kotlin.coroutines.cancellation.CancellationException

public interface CoreBluetoothPeripheral : Peripheral {

    /**
     * This is an internal API and may be removed from a future release. If you are using it, please
     * open an issue and report your use case.
     */
    @InternalKableApi
    public val cbPeripheral: CBPeripheral

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

    /**
     * Opens an L2CAP channel to the peripheral on the given [psm], suspending until CoreBluetooth
     * reports the channel open. The returned [L2CapSocket] is already connected.
     *
     * Reopening a [psm] shortly after its previous socket was closed can fail with an [L2CapException]
     * ("L2CAP PSM already connected") while CoreBluetooth is still tearing the old channel down.
     * Callers may retry.
     *
     * @see platform.CoreBluetooth.CBPeripheral.openL2CAPChannel
     * @throws L2CapException if the channel could not be opened.
     */
    @Throws(CancellationException::class, IOException::class)
    public suspend fun openL2CapChannel(psm: Int): L2CapSocket
}
