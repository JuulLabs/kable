package com.juul.kable

import com.juul.kable.external.BluetoothDevice
import com.juul.kable.interop.await
import kotlinx.coroutines.ensureActive
import web.errors.DOMException
import web.errors.SecurityError
import kotlin.coroutines.coroutineContext
import kotlin.js.JsException
import kotlin.js.thrownValue
import kotlin.js.toList

public actual fun Peripheral(
    advertisement: Advertisement,
    builderAction: PeripheralBuilderAction,
): Peripheral {
    advertisement as BluetoothAdvertisingEventWebBluetoothAdvertisement
    return Peripheral(advertisement.bluetoothDevice, builderAction)
}

@Suppress("FunctionName") // Builder function.
public suspend fun Peripheral(
    identifier: Identifier,
    builderAction: PeripheralBuilderAction,
): WebBluetoothPeripheral? {
    val bluetooth = bluetoothOrThrow()
    val devices = try {
        bluetooth.getDevices().await().toList()
    } catch (e: JsException) {
        coroutineContext.ensureActive()
        val thrownValue = e.thrownValue
        throw when {
            // The Web Bluetooth API can only be used in a secure context.
            // https://developer.mozilla.org/en-US/docs/Web/API/Web_Bluetooth_API#security_considerations
            thrownValue is DOMException && thrownValue.name == DOMException.SecurityError ->
                IllegalStateException("Operation is not permitted in this context due to security concerns", e)

            else -> InternalError("Failed to invoke getDevices request", e)
        }
    }
    return devices.singleOrNull { bluetoothDevice ->
        bluetoothDevice.id == identifier
    }?.let { bluetoothDevice ->
        Peripheral(bluetoothDevice, builderAction)
    }
}

@Suppress("FunctionName") // Builder function.
internal fun Peripheral(
    bluetoothDevice: BluetoothDevice,
    builderAction: PeripheralBuilderAction,
): WebBluetoothPeripheral = Peripheral(bluetoothDevice, PeripheralBuilder().apply(builderAction))

@Suppress("FunctionName") // Builder function.
internal fun Peripheral(
    bluetoothDevice: BluetoothDevice,
    builder: PeripheralBuilder,
): WebBluetoothPeripheral = builder.build(bluetoothDevice)
