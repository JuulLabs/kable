package com.juul.kable

import com.juul.kable.external.Bluetooth
import com.juul.kable.external.BluetoothAdvertisingEvent
import com.juul.kable.external.BluetoothLEScanOptions
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.w3c.dom.events.Event

private const val ADVERTISEMENT_RECEIVED_EVENT = "advertisementreceived"

/**
 * Only available on Chrome 79+ with "Experimental Web Platform features" enabled via:
 * chrome://flags/#enable-experimental-web-platform-features
 *
 * See also: [Chrome Platform Status: Web Bluetooth Scanning](https://www.chromestatus.com/feature/5346724402954240)
 */
public class JsScanner internal constructor(
    bluetooth: Bluetooth,
    filters: List<Filter>,
    logging: Logging,
) : Scanner {

    private val logger = Logger(logging, tag = "Kable/Scanner", identifier = null)

    // https://webbluetoothcg.github.io/web-bluetooth/scanning.html#scanning
    private val supportsScanning = js("window.navigator.bluetooth.requestLEScan") != null

    private val options = filters.toBluetoothLEScanOptions()

    public override val advertisements: Flow<Advertisement> = callbackFlow {
        check(supportsScanning) { "Scanning unavailable" }

        logger.info { message = "Starting scan" }
        console.info(options)
        val scan = bluetooth.requestLEScan(options).await()

        console.info("Configuring listener")
        val listener: (Event) -> Unit = { event ->
            console.info(event)
            trySend(Advertisement(event.unsafeCast<BluetoothAdvertisingEvent>())).getOrElse {
                console.warn("Unable to deliver advertisement event due to failure in flow or premature closing.")
            }
        }
        bluetooth.addEventListener(ADVERTISEMENT_RECEIVED_EVENT, listener)
        console.info("Listener configured")

        awaitClose {
            logger.info { message = "Stopping scan" }
            scan.stop()
            bluetooth.removeEventListener(ADVERTISEMENT_RECEIVED_EVENT, listener)
        }
    }
}

/** Convert list of public API type to Web Bluetooth (JavaScript) type. */
private fun List<Filter>.toBluetoothLEScanOptions(): BluetoothLEScanOptions = jso {
    if (this@toBluetoothLEScanOptions.isEmpty()) {
        acceptAllAdvertisements = true
    } else {
        filters = this@toBluetoothLEScanOptions.toBluetoothLEScanFilterInit()
    }
}
