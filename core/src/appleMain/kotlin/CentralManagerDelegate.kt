package com.juul.kable

import com.juul.kable.CentralManagerDelegate.ConnectionEvent.DidConnect
import com.juul.kable.CentralManagerDelegate.ConnectionEvent.DidDisconnect
import com.juul.kable.CentralManagerDelegate.ConnectionEvent.DidFailToConnect
import com.juul.kable.CentralManagerDelegate.Response.DidDiscoverPeripheral
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.runBlocking
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBManagerState
import platform.CoreBluetooth.CBPeripheral
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.NSUUID
import platform.darwin.NSObject
import kotlin.native.concurrent.freeze

internal class CentralManagerDelegate : NSObject(), CBCentralManagerDelegateProtocol {

    private val _state = MutableStateFlow<CBManagerState?>(null)
    val state: Flow<CBManagerState> = _state.filterNotNull()

    sealed class Response {

        data class DidDiscoverPeripheral(
            val peripheral: CBPeripheral,
            val rssi: NSNumber,
            val advertisementData: Map<String, Any>,
        ) : Response()
    }

    // todo: Use MutableSharedFlow when Kotlin/kotlinx.coroutines#2069 is released.
    // https://github.com/Kotlin/kotlinx.coroutines/pull/2069
    private val _response = BroadcastChannel<Response>(BUFFERED).freeze()
    val response: Flow<Response> = _response.asFlow().freeze()

    sealed class ConnectionEvent {

        abstract val identifier: NSUUID

        data class DidConnect(
            override val identifier: NSUUID
        ) : ConnectionEvent()

        data class DidFailToConnect(
            override val identifier: NSUUID,
            val error: NSError?,
        ) : ConnectionEvent()

        data class DidDisconnect(
            override val identifier: NSUUID,
            val error: NSError?,
        ) : ConnectionEvent()
    }

    // todo: Use MutableStateFlow when Kotlin/kotlinx.coroutines#2226 is fixed.
    // https://github.com/Kotlin/kotlinx.coroutines/issues/2226
    private val _connection = BroadcastChannel<ConnectionEvent>(CONFLATED).freeze()
    val connection: Flow<ConnectionEvent> = _connection.asFlow().freeze()

    override fun centralManager(
        central: CBCentralManager,
        didDiscoverPeripheral: CBPeripheral,
        advertisementData: Map<Any?, *>,
        RSSI: NSNumber
    ): Unit {
        val peripheral = didDiscoverPeripheral.freeze()

        // Per Apply documentation, defined as dictionary of `[String : Any]`.
        // https://developer.apple.com/documentation/corebluetooth/cbcentralmanagerdelegate/1518937-centralmanager
        val data = advertisementData as Map<String, Any>

        _response.sendBlocking(DidDiscoverPeripheral(peripheral, RSSI, data))
    }

    override fun centralManager(
        central: CBCentralManager,
        didConnectPeripheral: CBPeripheral
    ): Unit {
        _connection.offer(DidConnect(didConnectPeripheral.identifier))
    }

    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun centralManager(
        central: CBCentralManager,
        didFailToConnectPeripheral: CBPeripheral,
        error: NSError?
    ): Unit {
        _connection.offer(DidFailToConnect(didFailToConnectPeripheral.identifier, error))
    }

    // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    @Suppress("CONFLICTING_OVERLOADS")
    override fun centralManager(
        central: CBCentralManager,
        didDisconnectPeripheral: CBPeripheral,
        error: NSError?
    ): Unit {
        _connection.offer(DidDisconnect(didDisconnectPeripheral.identifier, error))
    }

    override fun centralManagerDidUpdateState(
        central: CBCentralManager
    ): Unit {
        _state.value = central.state
    }
}

private fun <E> SendChannel<E>.sendBlocking(element: E) {
    // fast path
    if (offer(element)) return

    // slow path
    runBlocking { send(element) }
}
