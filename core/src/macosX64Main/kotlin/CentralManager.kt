package com.juul.kable

import com.benasher44.uuid.Uuid
import kotlinx.coroutines.withContext
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBCharacteristicWriteType
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBService
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSData
import kotlin.native.concurrent.freeze

private const val DISPATCH_QUEUE_LABEL = "central"

public class CentralManager internal constructor() {

    internal companion object {
        val Default: CentralManager = CentralManager()
    }

    private val dispatcher = QueueDispatcher(DISPATCH_QUEUE_LABEL)
    internal val delegate = CentralManagerDelegate().freeze()
    private val cbCentralManager = CBCentralManager(delegate, dispatcher.dispatchQueue)

    internal suspend fun scanForPeripheralsWithServices(
        services: List<Uuid>?,
        options: Map<Any?, *>?,
    ) {
        println("-> CentralManager.scanForPeripheralsWithServices")
        withContext(dispatcher) {
            cbCentralManager.scanForPeripheralsWithServices(services, options)
        }
    }

    internal fun stopScan() {
        println("-> CentralManager.stopScan")
        cbCentralManager.stopScan()
    }

    internal suspend fun connectPeripheral(
        cbPeripheral: CBPeripheral,
        delegate: PeripheralDelegate,
        options: Map<Any?, *>? = null,
    ): Connection {
        println("-> CentralManager.connectPeripheral")
        withContext(dispatcher) {
            cbPeripheral.delegate = delegate
            cbCentralManager.connectPeripheral(cbPeripheral, options)
        }
        return Connection(delegate)
    }

    internal suspend fun cancelPeripheralConnection(
        cbPeripheral: CBPeripheral,
    ) {
        println("-> CentralManager.cancelPeripheralConnection")
        withContext(dispatcher) {
            cbCentralManager.cancelPeripheralConnection(cbPeripheral)
            cbPeripheral.delegate = null
        }
    }

    internal suspend fun readRssi(
        cbPeripheral: CBPeripheral,
    ) {
        println("-> CentralManager CBPeripheral.readRssi")
        withContext(dispatcher) {
            cbPeripheral.readRSSI()
        }
    }

    internal suspend fun discoverServices(
        cbPeripheral: CBPeripheral,
        services: List<CBUUID>?,
    ) {
        println("-> CentralManager CBPeripheral.discoverServices")
        withContext(dispatcher) {
            cbPeripheral.discoverServices(services)
        }
    }

    internal suspend fun discoverCharacteristics(
        cbPeripheral: CBPeripheral,
        cbService: CBService,
    ) {
        println("-> CentralManager CBPeripheral.discoverCharacteristics")
        withContext(dispatcher) {
            cbPeripheral.discoverCharacteristics(null, cbService)
        }
    }

    internal suspend fun write(
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

    internal suspend fun read(
        cbPeripheral: CBPeripheral,
        cbCharacteristic: CBCharacteristic,
    ) {
        println("-> CentralManager CBPeripheral.readValueForCharacteristic")
        withContext(dispatcher) {
            cbPeripheral.readValueForCharacteristic(cbCharacteristic)
        }
    }

    internal suspend fun write(
        cbPeripheral: CBPeripheral,
        data: NSData,
        cbDescriptor: CBDescriptor,
    ) {
        println("-> CentralManager CBPeripheral.writeValue(CBDescriptor)")
        withContext(dispatcher) {
            cbPeripheral.writeValue(data, cbDescriptor)
        }
    }

    internal suspend fun read(
        cbPeripheral: CBPeripheral,
        cbDescriptor: CBDescriptor,
    ) {
        withContext(dispatcher) {
            println("-> CentralManager CBPeripheral.readValueForDescriptor")
            cbPeripheral.readValueForDescriptor(cbDescriptor)
        }
    }

    internal suspend fun notify(
        cbPeripheral: CBPeripheral,
        cbCharacteristic: CBCharacteristic,
    ) {
        println("-> CentralManager CBPeripheral.setNotifyValue(true)")
        withContext(dispatcher) {
            cbPeripheral.setNotifyValue(true, cbCharacteristic)
        }
    }

    internal suspend fun cancelNotify(
        cbPeripheral: CBPeripheral,
        cbCharacteristic: CBCharacteristic,
    ) {
        println("-> CentralManager CBPeripheral.setNotifyValue(false)")
        withContext(dispatcher) {
            cbPeripheral.setNotifyValue(false, cbCharacteristic)
        }
    }
}
