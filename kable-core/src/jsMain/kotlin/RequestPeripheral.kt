package com.juul.kable

import com.juul.kable.logs.Logger
import js.errors.JsError
import js.errors.TypeError
import kotlinx.coroutines.await
import kotlinx.coroutines.ensureActive
import web.errors.DOMException
import web.errors.DOMException.Companion.NotFoundError
import web.errors.DOMException.Companion.SecurityError
import kotlin.coroutines.coroutineContext

/**
 * Obtains a nearby [Peripheral] via device picker. Returns `null` if dialog is cancelled (e.g. user
 * dismissed dialog by clicking outside of dialog or clicking cancel button).
 *
 * See [requestDevice](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth/requestDevice) for
 * more details.
 *
 * Throws [IllegalStateException] under the following conditions:
 * - Bluetooth is unavailable
 * - Requesting a device is [not supported](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth/requestDevice#browser_compatibility)
 * - Operation is not permitted due to [security concerns](https://developer.mozilla.org/en-US/docs/Web/API/Web_Bluetooth_API#security_considerations)
 *
 * @throws IllegalStateException If requesting a device could not be fulfilled.
 */
public suspend fun requestPeripheral(
    options: Options,
    builderAction: PeripheralBuilderAction = {},
): Peripheral? {
    val bluetooth = bluetoothOrThrow()
    val requestDeviceOptions = options.toRequestDeviceOptions()
    val requestDevice = try {
        bluetooth.requestDevice(requestDeviceOptions)
    } catch (e: JsError) {
        coroutineContext.ensureActive()
        throw when (e) {
            is TypeError -> IllegalStateException("Requesting a device is not supported", e)
            else -> InternalException("Failed to invoke device request", e)
        }
    }

    // Acquire/configure `PeripheralBuilder` early (diverging from typical
    // `PeripheralBuilder().apply(..).build(..)` pattern), so that `Logger` is available (if needed).
    val builder = PeripheralBuilder().apply(builderAction)

    return try {
        requestDevice.await()
    } catch (e: JsError) {
        coroutineContext.ensureActive()
        when {
            // User cancelled picker dialog by either clicking outside dialog, or clicking cancel button.
            e is DOMException && e.name == NotFoundError -> null

            // The Web Bluetooth API can only be used in a secure context.
            // https://developer.mozilla.org/en-US/docs/Web/API/Web_Bluetooth_API#security_considerations
            e is DOMException && e.name == SecurityError ->
                throw IllegalStateException("Operation is not permitted in this context due to security concerns", e)

            e is TypeError -> {
                // Example failure when executing `requestDevice(jso {})`:
                // > TypeError: Failed to execute 'requestDevice' on 'Bluetooth': Either 'filters'
                // > should be present or 'acceptAllAdvertisements' should be true, but not both.
                //
                // We expect valid `options` to be produced; if that isn't the case, then we log and
                // throw `InternalError` (in hopes we get a bug report).
                val logger = Logger(builder.logging, tag = "Kable/requestDevice", identifier = null)
                logger.error {
                    detail("options", options.toString())
                    detail("processed", JSON.stringify(requestDeviceOptions))
                    message = e.toString()
                }
                throw InternalException("Type error when requesting device", e)
            }

            else -> throw InternalException("Failed to request device", e)
        }
    }?.let(builder::build)
}
