package com.juul.kable

import com.juul.kable.CentralManagerDelegate.ConnectionEvent.DidConnect
import com.juul.kable.CentralManagerDelegate.ConnectionEvent.DidDisconnect
import com.juul.kable.CentralManagerDelegate.ConnectionEvent.DidFailToConnect
import com.juul.kable.CentralManagerDelegate.Response.DidDiscoverPeripheral
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBManagerState
import platform.CoreBluetooth.CBPeripheral
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.NSUUID
import platform.darwin.NSObject

/*
TBD Not implemented yet.  This will handle CBManagerWillRestoreState
public data class CBManagerRestoredState(
    val peripherals: List<CBPeripheral>? = null,
    val scannUUIDs: List<CBUUID>? = null,
    //TBD Scan Options
)

*/

// https://developer.apple.com/documentation/corebluetooth/cbcentralmanagerdelegate
internal class CentralManagerDelegate : NSObject(), CBCentralManagerDelegateProtocol {

    private val _onDisconnected = MutableSharedFlow<NSUUID>()
    internal val onDisconnected = _onDisconnected.asSharedFlow()
    private val _state = MutableStateFlow<CBManagerState?>(null)
    val state: Flow<CBManagerState> = _state.filterNotNull()

    /*
    TBD Not implemented yet.  This will handle CBManagerWillRestoreState
    private val _willRestoreStateFlow = MutableSharedFlow<CBManagerRestoredState?>()
    */

    sealed class Response {
        data class DidDiscoverPeripheral(
            val cbPeripheral: CBPeripheral,
            val rssi: NSNumber,
            val advertisementData: Map<String, Any>,
        ) : Response()
    }

    private val _response = MutableSharedFlow<Response>(extraBufferCapacity = 64)
    val response: Flow<Response> = _response.asSharedFlow()

    sealed class ConnectionEvent {

        abstract val identifier: NSUUID

        data class DidConnect(
            override val identifier: NSUUID,
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

    private val _connectionState = MutableStateFlow<ConnectionEvent?>(null)
    val connectionState: Flow<ConnectionEvent> = _connectionState.filterNotNull()

    /* Monitoring Connections with Peripherals */

    override fun centralManager(
        central: CBCentralManager,
        didConnectPeripheral: CBPeripheral,
    ) {
        _connectionState.value = DidConnect(didConnectPeripheral.identifier)
    }

    @Suppress("CONFLICTING_OVERLOADS") // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    override fun centralManager(
        central: CBCentralManager,
        didDisconnectPeripheral: CBPeripheral,
        error: NSError?,
    ) {
        _onDisconnected.emitBlocking(didDisconnectPeripheral.identifier) // Used to notify `Peripheral` of disconnect.
        _connectionState.value = DidDisconnect(didDisconnectPeripheral.identifier, error)
    }

    @Suppress("CONFLICTING_OVERLOADS") // https://kotlinlang.org/docs/reference/native/objc_interop.html#subclassing-swiftobjective-c-classes-and-protocols-from-kotlin
    override fun centralManager(
        central: CBCentralManager,
        didFailToConnectPeripheral: CBPeripheral,
        error: NSError?,
    ) {
        _connectionState.value = DidFailToConnect(didFailToConnectPeripheral.identifier, error)
    }

    // todo: func centralManager(CBCentralManager, connectionEventDidOccur: CBConnectionEvent, for: CBPeripheral)

    /* Discovering and Retrieving Peripherals */

    override fun centralManager(
        central: CBCentralManager,
        didDiscoverPeripheral: CBPeripheral,
        advertisementData: Map<Any?, *>,
        RSSI: NSNumber,
    ) {
        // Per Apple documentation, `advertisementData` is defined as dictionary of `[String : Any]`.
        // https://developer.apple.com/documentation/corebluetooth/cbcentralmanagerdelegate/1518937-centralmanager
        val data = advertisementData as Map<String, Any>

        _response.emitBlocking(DidDiscoverPeripheral(didDiscoverPeripheral, RSSI, data))
    }

    /* Monitoring the Central Manager’s State */

    override fun centralManagerDidUpdateState(
        central: CBCentralManager,
    ) {
        _state.value = central.state
    }

 /*     TODO: The willRestoreState requires the CentralManager to be instantiated with identifier
          This will need to be done before enabling this method
    override fun centralManager(central: CBCentralManager, willRestoreState: Map<Any?, *>){
        val peripherals =
                willRestoreState[CBCentralManagerRestoredStatePeripheralsKey] as List<CBPeripheral>?
        val scanUUIDs =
                willRestoreState[CBCentralManagerRestoredStateScanServicesKey] as List<CBUUID>?

        _willRestoreStateFlow.tryEmit(CBManagerRestoredState(
            peripherals, scanUUIDs))
    }
    */

    /* Monitoring the Central Manager’s Authorization */

    // todo: func centralManager(CBCentralManager, didUpdateANCSAuthorizationFor: CBPeripheral)
}
