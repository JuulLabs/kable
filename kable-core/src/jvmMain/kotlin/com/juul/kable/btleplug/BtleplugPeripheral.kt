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
import com.juul.kable.btleplug.ffi.PeripheralCallbacks
import com.juul.kable.btleplug.ffi.Uuid
import com.juul.kable.btleplug.ffi.getPeripheral
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.sharedRepeatableAction
import com.juul.kable.suspendUntil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.io.IOException
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

    private val callbacks = object : PeripheralCallbacks {
        override fun connected() {
            _state.value = State.Connecting.Services
        }

        override fun disconnected() {
            _state.value = Disconnected()
        }

        override fun notification(uuid: Uuid, data: ByteArray) {
            // TODO: Not yet implemented
        }
    }

    private val peripheral = scope.async { getPeripheral(identifier, callbacks) }

    override val name: String?
        get() = runBlocking { peripheral.await().properties().localName }

    private suspend fun establishConnection(scope: CoroutineScope): CoroutineScope {
        // TODO: Check Bluetooth is on/supported

        logger.info { message = "Connecting" }
        _state.value = State.Connecting.Bluetooth

        if (!peripheral.await().connect()) {
            throw IOException("Failed to connect.")
        }
        suspendUntil<State.Connecting.Services>()
        if (!peripheral.await().discoverServices()) {
            throw IOException("Failed to discover services")
        }
        _state.value = State.Connecting.Observes
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
    override suspend fun rssi(): Int =
        peripheral.await().properties().rssi?.toInt() ?: Int.MIN_VALUE

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
