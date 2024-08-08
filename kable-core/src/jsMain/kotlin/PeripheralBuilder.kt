package com.juul.kable

import com.juul.kable.external.BluetoothDevice
import com.juul.kable.logs.Logging
import com.juul.kable.logs.LoggingBuilder
import kotlinx.coroutines.CoroutineScope

public actual class ServicesDiscoveredPeripheral internal constructor(
    private val peripheral: WebBluetoothPeripheral,
) {

    public actual suspend fun read(
        characteristic: Characteristic,
    ): ByteArray = peripheral.read(characteristic)

    public actual suspend fun read(
        descriptor: Descriptor,
    ): ByteArray = peripheral.read(descriptor)

    public actual suspend fun write(
        characteristic: Characteristic,
        data: ByteArray,
        writeType: WriteType,
    ) {
        peripheral.write(characteristic, data, writeType)
    }

    public actual suspend fun write(
        descriptor: Descriptor,
        data: ByteArray,
    ) {
        peripheral.write(descriptor, data)
    }
}

public actual class PeripheralBuilder internal actual constructor() {

    internal var logging: Logging = Logging()
    public actual fun logging(init: LoggingBuilder) {
        val logging = Logging()
        logging.init()
        this.logging = logging
    }

    internal var onServicesDiscovered: ServicesDiscoveredAction = {}
    public actual fun onServicesDiscovered(action: ServicesDiscoveredAction) {
        onServicesDiscovered = action
    }

    internal var observationExceptionHandler: ObservationExceptionHandler = { cause -> throw cause }
    public actual fun observationExceptionHandler(handler: ObservationExceptionHandler) {
        observationExceptionHandler = handler
    }

    internal fun build(bluetoothDevice: BluetoothDevice, scope: CoroutineScope) =
        BluetoothDeviceWebBluetoothPeripheral(
            scope.coroutineContext,
            bluetoothDevice,
            observationExceptionHandler,
            onServicesDiscovered,
            logging,
        )
}
