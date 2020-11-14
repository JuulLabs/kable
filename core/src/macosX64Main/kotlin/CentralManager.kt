package com.juul.kable

import com.benasher44.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBCharacteristicWriteType
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBService
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSData
import kotlin.coroutines.CoroutineContext
import kotlin.native.concurrent.freeze

private const val DISPATCH_QUEUE_LABEL = "central"

internal fun CoroutineScope.centralManager(): CentralManager = CentralManager(coroutineContext)

internal class CentralManager(
    parentCoroutineContext: CoroutineContext,
) {

    private val job = Job(parentCoroutineContext[Job]).apply {
        invokeOnCompletion {
            println("CentralManager job invokeOnCompletion")
            cbCentralManager.delegate = null
            delegate.peripheralDelegates.clear()
        }
    }
    private val dispatcher = QueueDispatcher(DISPATCH_QUEUE_LABEL)
    private val scope = CoroutineScope(parentCoroutineContext + job + dispatcher)

    internal val delegate = CentralManagerDelegate().freeze()
    private val cbCentralManager = CBCentralManager(delegate, dispatcher.dispatchQueue)

    fun scanForPeripheralsWithServices(
        services: List<Uuid>?,
        options: Map<Any?, *>?,
    ) {
        println("-> CentralManager.scanForPeripheralsWithServices")
        scope.launch {
            cbCentralManager.scanForPeripheralsWithServices(services, options)
        }
    }

    fun stopScan() {
        println("-> CentralManager.stopScan")
        cbCentralManager.stopScan()
    }

    suspend fun connectPeripheral(
        cbPeripheral: CBPeripheral,
        delegate: PeripheralDelegate,
        options: Map<Any?, *>? = null,
    ): Connection {
        println("-> CentralManager.connectPeripheral")
        withContext(dispatcher) {
            cbPeripheral.delegate = delegate
            this@CentralManager.delegate.peripheralDelegates.put(cbPeripheral.identifier, delegate)
            cbCentralManager.connectPeripheral(cbPeripheral, options)
        }
        return Connection(delegate)
    }

    suspend fun cancelPeripheralConnection(
        cbPeripheral: CBPeripheral,
    ) {
        println("-> CentralManager.cancelPeripheralConnection")
        withContext(dispatcher) {
            cbCentralManager.cancelPeripheralConnection(cbPeripheral)
            cbPeripheral.delegate = null
        }
    }

    suspend fun readRssi(
        cbPeripheral: CBPeripheral,
    ) {
        println("-> CentralManager CBPeripheral.readRssi")
        withContext(dispatcher) {
            cbPeripheral.readRSSI()
        }
    }

    suspend fun discoverServices(
        cbPeripheral: CBPeripheral,
        services: List<CBUUID>?,
    ) {
        println("-> CentralManager CBPeripheral.discoverServices")
        withContext(dispatcher) {
            cbPeripheral.discoverServices(services)
        }
    }

    suspend fun discoverCharacteristics(
        cbPeripheral: CBPeripheral,
        cbService: CBService,
    ) {
        println("-> CentralManager CBPeripheral.discoverCharacteristics")
        withContext(dispatcher) {
            cbPeripheral.discoverCharacteristics(null, cbService)
        }
    }

    suspend fun write(
        cbPeripheral: CBPeripheral,
        data: NSData,
        cbCharacteristic: CBCharacteristic,
        cbWriteType: CBCharacteristicWriteType,
    ) {
        println("-> CentralManager CBPeripheral.writeValue(CBCharacteristic)")
        withContext(dispatcher) {
            cbPeripheral.writeValue(data, cbCharacteristic, cbWriteType)
        }
    }

    suspend fun read(
        cbPeripheral: CBPeripheral,
        cbCharacteristic: CBCharacteristic,
    ) {
        println("-> CentralManager CBPeripheral.readValueForCharacteristic")
        withContext(dispatcher) {
            cbPeripheral.readValueForCharacteristic(cbCharacteristic)
        }
    }

    suspend fun write(
        cbPeripheral: CBPeripheral,
        data: NSData,
        cbDescriptor: CBDescriptor,
    ) {
        println("-> CentralManager CBPeripheral.writeValue(CBDescriptor)")
        withContext(dispatcher) {
            cbPeripheral.writeValue(data, cbDescriptor)
        }
    }

    suspend fun read(
        cbPeripheral: CBPeripheral,
        cbDescriptor: CBDescriptor,
    ) {
        withContext(dispatcher) {
            println("-> CentralManager CBPeripheral.readValueForDescriptor")
            cbPeripheral.readValueForDescriptor(cbDescriptor)
        }
    }

    suspend fun notify(
        cbPeripheral: CBPeripheral,
        cbCharacteristic: CBCharacteristic,
    ) {
        println("-> CentralManager CBPeripheral.setNotifyValue(true)")
        withContext(dispatcher) {
            cbPeripheral.setNotifyValue(true, cbCharacteristic)
        }
    }

    suspend fun cancelNotify(
        cbPeripheral: CBPeripheral,
        cbCharacteristic: CBCharacteristic,
    ) {
        println("-> CentralManager CBPeripheral.setNotifyValue(false)")
        withContext(dispatcher) {
            cbPeripheral.setNotifyValue(false, cbCharacteristic)
        }
    }
}
