package com.juul.kable

import com.juul.kable.external.BluetoothAdvertisingEvent
import com.juul.kable.interop.await
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import js.errors.TypeError
import js.json.stringify
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import web.errors.DOMException
import web.errors.SecurityError
import web.events.EventType
import web.events.addEventListener
import web.events.removeEventListener
import kotlin.js.JsException
import kotlin.js.thrownValue

private val ADVERTISEMENT_RECEIVED_EVENT = EventType<BluetoothAdvertisingEvent>("advertisementreceived")

/**
 * Only available on Chrome 79+ with "Experimental Web Platform features" enabled via:
 * chrome://flags/#enable-experimental-web-platform-features
 *
 * See also: [Chrome Platform Status: Web Bluetooth Scanning](https://www.chromestatus.com/feature/5346724402954240)
 *
 * @throws IllegalArgumentException If `filters` argument contains a filter of type [Filter.Address].
 * @throws IllegalStateException If bluetooth is not available.
 */
internal class BluetoothWebBluetoothScanner(
    filters: List<FilterPredicate>,
    logging: Logging,
) : PlatformScanner {

    init {
        require(filters.flatten().none { it is Filter.Address }) {
            "Filtering by address (`Filter.Address`) is not supported on Javascript platforms"
        }
    }

    private val logger = Logger(logging, tag = "Kable/Scanner", identifier = null)
    private val options = filters.toBluetoothLEScanOptions()

    /**
     * @throws IllegalStateException If bluetooth is [unavailable](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth#browser_compatibility),
     * or scanning is [not supported](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth/requestDevice#browser_compatibility)
     * or operation is not permitted due to [security concerns](https://developer.mozilla.org/en-US/docs/Web/API/Web_Bluetooth_API#security_considerations).
     */
    override val advertisements: Flow<PlatformAdvertisement> = callbackFlow {
        logger.verbose { message = "Initializing scan" }
        val bluetooth = bluetoothOrThrow()

        val requestLEScan = try {
            bluetooth.requestLEScan(options)
        } catch (e: JsException) {
            if (e.thrownValue is TypeError) {
                // Example failure when executing `requestLEScan(..)` with Chrome's "Experimental Web Platform features" turned off:
                // > TypeError: navigator.bluetooth.requestLEScan is not a function
                throw IllegalStateException("Scanning not supported", e)
            }
            ensureActive()
            throw InternalError("Failed to request scan", e)
        }

        logger.verbose { message = "Adding scan listener" }
        val listener: (BluetoothAdvertisingEvent) -> Unit = {
            val advertisement = BluetoothAdvertisingEventWebBluetoothAdvertisement(it)
            trySend(advertisement).getOrElse {
                logger.warn { message = "Unable to deliver advertisement event due to failure in flow or premature closing." }
            }
        }
        bluetooth.addEventListener(ADVERTISEMENT_RECEIVED_EVENT, listener)

        logger.info { message = "Starting scan" }
        val scan = try {
            requestLEScan.await()
        } catch (e: JsException) {
            logger.verbose { message = "Removing scan listener" }
            bluetooth.removeEventListener(ADVERTISEMENT_RECEIVED_EVENT, listener)
            ensureActive()

            // The Web Bluetooth API can only be used in a secure context.
            // https://developer.mozilla.org/en-US/docs/Web/API/Web_Bluetooth_API#security_considerations
            val thrownValue = e.thrownValue
            if (thrownValue is DOMException && thrownValue.name == DOMException.SecurityError) {
                throw IllegalStateException("Operation is not permitted in this context due to security concerns", e)
            }

            // Example failure when executing `requestLEScan(unsafeJso {})`:
            // > TypeError: Failed to execute 'requestLEScan' on 'Bluetooth': Either 'filters' should be present or 'acceptAllAdvertisements' should be true, but not both.
            //
            // Based on the input `filters`, we expect valid `options` to be produced; if that isn't
            // the case, then we log and throw `InternalError` (in hopes we'll get a bug report so
            // that we can fix any issues).
            logger.error {
                detail("filters", filters.toString())
                detail("options", stringify(options))
                message = e.toString()
            }
            throw InternalError("Failed to start scan", e)
        }

        awaitClose {
            logger.info { message = "Stopping scan" }
            scan.stop()
            logger.verbose { message = "Removing scan listener" }
            bluetooth.removeEventListener(ADVERTISEMENT_RECEIVED_EVENT, listener)
        }
    }
}
