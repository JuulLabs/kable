package com.juul.kable

import com.benasher44.uuid.Uuid
import com.juul.kable.logs.Logging
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBCharacteristicWriteType
import platform.CoreBluetooth.CBDescriptor
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBService
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSData

private const val DISPATCH_QUEUE_LABEL = "central"

public actual class CentralManager actual constructor(
    options: Map<Any?, *>?,
) {

    public actual companion object {

        private val _Default = atomic<CentralManager?>(null)
        internal val Default: CentralManager
            get() = _Default.value ?: error("CentralManager has not been initialized.")

        public actual fun initialize(builderAction: CentralBuilder.() -> Unit) {
            val newValue = CentralBuilder().apply(builderAction).build()
            if (!_Default.compareAndSet(null, newValue)) error("CentralManager already initialized.")
        }
    }

    private val dispatcher = QueueDispatcher(DISPATCH_QUEUE_LABEL)
    internal val delegate = CentralManagerDelegate()
    private val cbCentralManager = CBCentralManager(delegate, dispatcher.dispatchQueue, options)

    internal suspend fun scanForPeripheralsWithServices(
        services: List<Uuid>?,
        options: Map<Any?, *>?,
    ) {
        withContext(dispatcher) {
            cbCentralManager.scanForPeripheralsWithServices(
                serviceUUIDs = services?.map { it.toCBUUID() },
                options = options,
            )
        }
    }

    internal fun stopScan() {
        // Check scanning state to prevent API misuse warning.
        // https://github.com/JuulLabs/kable/issues/81
        if (cbCentralManager.isScanning) cbCentralManager.stopScan()
    }

    internal fun retrievePeripheral(withIdentifier: Uuid): CBPeripheral? =
        cbCentralManager
            .retrievePeripheralsWithIdentifiers(listOf(withIdentifier.toNSUUID()))
            .firstOrNull() as? CBPeripheral

    internal suspend fun connectPeripheral(
        scope: CoroutineScope,
        peripheral: CBPeripheralCoreBluetoothPeripheral,
        characteristicChanges: MutableSharedFlow<ObservationEvent<NSData>>,
        logging: Logging,
        options: Map<Any?, *>? = null,
    ): Connection {
        val cbPeripheral = peripheral.cbPeripheral
        val identifier = cbPeripheral.identifier.UUIDString
        val delegate = PeripheralDelegate(peripheral.canSendWriteWithoutResponse, characteristicChanges, logging, identifier)
        withContext(dispatcher) {
            cbPeripheral.delegate = delegate
            cbCentralManager.connectPeripheral(cbPeripheral, options)
        }
        return Connection(scope, delegate, logging, identifier)
    }

    internal suspend fun cancelPeripheralConnection(
        cbPeripheral: CBPeripheral,
    ) {
        withContext(dispatcher) {
            cbCentralManager.cancelPeripheralConnection(cbPeripheral)
            cbPeripheral.delegate = null
        }
    }

    internal suspend fun readRssi(
        cbPeripheral: CBPeripheral,
    ) {
        withContext(dispatcher) {
            cbPeripheral.readRSSI()
        }
    }

    internal suspend fun discoverServices(
        cbPeripheral: CBPeripheral,
        services: List<CBUUID>?,
    ) {
        withContext(dispatcher) {
            cbPeripheral.discoverServices(services)
        }
    }

    internal suspend fun discoverCharacteristics(
        cbPeripheral: CBPeripheral,
        cbService: CBService,
    ) {
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
        withContext(dispatcher) {
            cbPeripheral.writeValue(data, cbCharacteristic, cbWriteType)
        }
    }

    internal suspend fun read(
        cbPeripheral: CBPeripheral,
        cbCharacteristic: CBCharacteristic,
    ) {
        withContext(dispatcher) {
            cbPeripheral.readValueForCharacteristic(cbCharacteristic)
        }
    }

    internal suspend fun write(
        cbPeripheral: CBPeripheral,
        data: NSData,
        cbDescriptor: CBDescriptor,
    ) {
        withContext(dispatcher) {
            cbPeripheral.writeValue(data, cbDescriptor)
        }
    }

    internal suspend fun read(
        cbPeripheral: CBPeripheral,
        cbDescriptor: CBDescriptor,
    ) {
        withContext(dispatcher) {
            cbPeripheral.readValueForDescriptor(cbDescriptor)
        }
    }

    internal suspend fun notify(
        cbPeripheral: CBPeripheral,
        cbCharacteristic: CBCharacteristic,
    ) {
        withContext(dispatcher) {
            cbPeripheral.setNotifyValue(true, cbCharacteristic)
        }
    }

    internal suspend fun cancelNotify(
        cbPeripheral: CBPeripheral,
        cbCharacteristic: CBCharacteristic,
    ) {
        withContext(dispatcher) {
            cbPeripheral.setNotifyValue(false, cbCharacteristic)
        }
    }
}
