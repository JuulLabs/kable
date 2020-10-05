package com.juul.kable

import com.benasher44.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBCharacteristicWriteType
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBService
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSData
import platform.Foundation.NSLog
import kotlin.coroutines.CoroutineContext
import kotlin.native.concurrent.freeze

private const val DISPATCH_QUEUE_LABEL = "central"

internal fun CoroutineScope.centralManager(): CentralManager = CentralManager(coroutineContext)

internal class CentralManager(
    parentCoroutineContext: CoroutineContext,
) {

    private val job = Job(parentCoroutineContext[Job]).apply {
        invokeOnCompletion {
            cbCentralManager.delegate = null
            observers.dispose()
        }
    }
    private val dispatcher: QueueDispatcher = QueueDispatcher(DISPATCH_QUEUE_LABEL)
    private val scope = CoroutineScope(parentCoroutineContext + job + dispatcher)

    internal val delegate = CentralManagerDelegate().freeze()
    private val cbCentralManager = CBCentralManager(delegate, dispatcher.dispatchQueue)

    fun scanForPeripheralsWithServices(
        services: List<Uuid>?,
        options: Map<Any?, *>?,
    ) {
        scope.launch {
            println("CentralManager scanForPeripheralsWithServices")
            cbCentralManager.scanForPeripheralsWithServices(services, options)
        }
    }

    fun stopScan() {
        println("CentralManager stopScan")
        cbCentralManager.stopScan()
    }

    suspend fun connectPeripheral(
        cbPeripheral: CBPeripheral,
        delegate: PeripheralDelegate,
        options: Map<Any?, *>?,
    ) {
        withContext(dispatcher) {
            println("CentralManager delegate")
            cbPeripheral.delegate = delegate
            println("CentralManager connectPeripheral")
            cbCentralManager.connectPeripheral(cbPeripheral, options)
        }
    }

    suspend fun cancelPeripheralConnection(
        cbPeripheral: CBPeripheral,
    ): Unit {
        withContext(dispatcher) {
            println("CentralManager cancelPeripheralConnection")
            cbCentralManager.cancelPeripheralConnection(cbPeripheral)

            println("CentralManager cancelPeripheralConnection delegate")
            cbPeripheral.delegate = null
        }
    }

    suspend fun readRssi(
        cbPeripheral: CBPeripheral,
    ): Unit {
        withContext(dispatcher) {
            cbPeripheral.readRSSI()
        }
    }

    suspend fun discoverServices(
        cbPeripheral: CBPeripheral,
        services: List<CBUUID>?,
    ): Unit {
        withContext(dispatcher) {
            cbPeripheral.discoverServices(services)
        }
    }

    suspend fun discoverCharacteristics(
        cbPeripheral: CBPeripheral,
        service: CBService,
    ): Unit {
        withContext(dispatcher) {
            cbPeripheral.discoverCharacteristics(null, service)
        }
    }

    suspend fun write(
        cbPeripheral: CBPeripheral,
        data: NSData,
        characteristic: CBCharacteristic,
        writeType: CBCharacteristicWriteType
    ) {
        withContext(dispatcher) {
            cbPeripheral.writeValue(data, characteristic, writeType)
        }
    }

    suspend fun read(
        cbPeripheral: CBPeripheral,
        characteristic: CBCharacteristic,
    ) {
        withContext(dispatcher) {
            cbPeripheral.readValueForCharacteristic(characteristic)
        }
    }

    private val observers = Observers()

    suspend fun notify(
        cbPeripheral: CBPeripheral,
        cbCharacteristic: CBCharacteristic,
    ) {
        withContext(dispatcher) {
            NSLog("Observe ${cbCharacteristic.UUID}")
            if (observers.increment(cbCharacteristic.UUID) == 1) {
                println("setNotifyValue(true, ${cbCharacteristic.UUID})")
                cbPeripheral.setNotifyValue(true, cbCharacteristic)
            }
        }
    }

    suspend fun cancelNotify(
        cbPeripheral: CBPeripheral,
        characteristic: Characteristic,
    ) {
        withContext(dispatcher) {
            val cbCharacteristic = cbCharacteristicFrom(peripheral, characteristic)
            if (observers.decrement(cbCharacteristic.UUID) == 0) {
                cbPeripheral.setNotifyValue(false, cbCharacteristic)
            }
        }
    }
}
