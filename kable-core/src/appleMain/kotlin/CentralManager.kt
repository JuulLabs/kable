package com.juul.kable

import com.juul.kable.logs.Logging
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBCharacteristicWriteType
import platform.CoreBluetooth.CBPeripheral
import platform.Foundation.NSData
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.uuid.Uuid

private const val DISPATCH_QUEUE_LABEL = "central"

public class CentralManager internal constructor(
    options: Map<Any?, *>?,
) {

    public data class Configuration(
        val stateRestoration: Boolean,
    ) {

        public class Builder internal constructor() {

            /**
             * Enables support for
             * [Core Bluetooth Background Processing for iOS Apps](https://developer.apple.com/library/archive/documentation/NetworkingInternetWeb/Conceptual/CoreBluetooth_concepts/CoreBluetoothBackgroundProcessingForIOSApps/PerformingTasksWhileYourAppIsInTheBackground.html)
             * by enabling [CentralManager] state preservation and restoration. */
            public var stateRestoration: Boolean = false
        }
    }

    public companion object {

        private val configuration = atomic<Configuration?>(null)
        private val lazyDefault = lazy { CentralManager(configuration.value?.toOptions()) }
        internal val Default by lazyDefault

        public fun configure(builderAction: Configuration.Builder.() -> Unit) {
            check(!lazyDefault.isInitialized()) { "Cannot configure CentralManager, it has already initialized." }
            configuration.value = Configuration.Builder().apply(builderAction).build()
        }
    }

    internal val dispatcher = QueueDispatcher(DISPATCH_QUEUE_LABEL)
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
        coroutineContext: CoroutineContext,
        peripheral: CBPeripheral,
        delegate: PeripheralDelegate,
        state: MutableStateFlow<State>,
        services: MutableStateFlow<List<PlatformDiscoveredService>?>,
        disconnectTimeout: Duration,
        logging: Logging,
        options: Map<Any?, *>? = null,
    ): Connection {
        withContext(dispatcher) {
            peripheral.delegate = delegate
            cbCentralManager.connectPeripheral(peripheral, options)
        }
        return Connection(
            coroutineContext,
            this,
            peripheral,
            delegate,
            state,
            services,
            disconnectTimeout,
            delegate.identifier,
            logging,
        )
    }

    internal suspend fun cancelPeripheralConnection(
        cbPeripheral: CBPeripheral,
    ) {
        withContext(dispatcher) {
            cbCentralManager.cancelPeripheralConnection(cbPeripheral)
            cbPeripheral.delegate = null
        }
    }

    internal suspend fun writeValue(
        cbPeripheral: CBPeripheral,
        data: NSData,
        cbCharacteristic: CBCharacteristic,
        cbWriteType: CBCharacteristicWriteType,
    ) {
        withContext(dispatcher) {
            cbPeripheral.writeValue(data, cbCharacteristic, cbWriteType)
        }
    }

    internal suspend fun readValue(
        cbPeripheral: CBPeripheral,
        cbCharacteristic: CBCharacteristic,
    ) {
        withContext(dispatcher) {
            cbPeripheral.readValueForCharacteristic(cbCharacteristic)
        }
    }
}
