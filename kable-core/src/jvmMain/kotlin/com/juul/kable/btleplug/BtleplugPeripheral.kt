package com.juul.kable.btleplug

import com.juul.kable.BasePeripheral
import com.juul.kable.Characteristic
import com.juul.kable.Descriptor
import com.juul.kable.DiscoveredService
import com.juul.kable.ExperimentalApi
import com.juul.kable.Identifier
import com.juul.kable.NotConnectedException
import com.juul.kable.OnSubscriptionAction
import com.juul.kable.State
import com.juul.kable.State.Disconnected
import com.juul.kable.WriteType
import com.juul.kable.awaitConnect
import com.juul.kable.bluetooth.checkBluetoothIsSupported
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.sharedRepeatableAction
import com.juul.kable.suspendUntil
import com.juul.kable.unwrapCancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import sun.util.logging.resources.logging
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalApi::class)
internal class BtleplugPeripheral(
    override val identifier: Identifier,
    logging: Logging,
) : BasePeripheral(identifier) {

    private val logger = Logger(logging, "Kable/Peripheral", identifier.toString())
    private val connectAction = scope.sharedRepeatableAction(::establishConnection)

    private val _state = MutableStateFlow<State>(Disconnected())
    override val state = _state.asStateFlow()

    override val name: String?
        get() = TODO("Not yet implemented")


    private suspend fun establishConnection(scope: CoroutineScope): CoroutineScope {
        // TODO: Check Bluetooth is on/supported

        logger.info { message = "Connecting" }
        _state.value = State.Connecting.Bluetooth


        // TODO: Physically connect
        suspendUntil<State.Connecting.Services>()
        // TODO: discoverServices()
        // TODO: configureCharacteristicObservations()

        return scope
    }

    override suspend fun connect(): CoroutineScope =
        connectAction.awaitConnect()

    override suspend fun disconnect() {
        connectAction.cancelAndJoin(CancellationException(NotConnectedException("Disconnect requested")))
    }

    override val services: StateFlow<List<DiscoveredService>?>
        get() = TODO("Not yet implemented")

    override suspend fun maximumWriteValueLengthForType(writeType: WriteType): Int {
        TODO("Not yet implemented")
    }

    @ExperimentalApi
    override suspend fun rssi(): Int {
        TODO("Not yet implemented")
    }

    override suspend fun read(characteristic: Characteristic): ByteArray {
        TODO("Not yet implemented")
    }

    override suspend fun read(descriptor: Descriptor): ByteArray {
        TODO("Not yet implemented")
    }

    override suspend fun write(characteristic: Characteristic, data: ByteArray, writeType: WriteType) {
        TODO("Not yet implemented")
    }

    override suspend fun write(descriptor: Descriptor, data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun observe(characteristic: Characteristic, onSubscription: OnSubscriptionAction): Flow<ByteArray> {
        TODO("Not yet implemented")
    }

    override fun close() {
        scope.cancel("$this closed")
    }

    override fun toString(): String = "Peripheral(identifier=$identifier)"
}
