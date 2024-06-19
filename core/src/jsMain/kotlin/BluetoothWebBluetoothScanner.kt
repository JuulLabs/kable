package com.juul.kable

import com.juul.kable.external.Bluetooth
import com.juul.kable.external.BluetoothAdvertisingEvent
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
internal class BluetoothWebBluetoothScanner(
    bluetooth: Bluetooth,
    filters: FilterPredicateSet,
    logging: Logging,
) : PlatformScanner {

    init {
        require(filters.flatten().none { it is Filter.Address }) {
            "Filtering by address (`Filter.Address`) is not supported on Javascript platforms"
        }
    }

    private val logger = Logger(logging, tag = "Kable/Scanner", identifier = null)

    // https://webbluetoothcg.github.io/web-bluetooth/scanning.html#scanning
    private val supportsScanning = js("window.navigator.bluetooth.requestLEScan") != null

    private val options = filters.toBluetoothLEScanOptions()

    override val advertisements: Flow<PlatformAdvertisement> = callbackFlow {
        check(supportsScanning) { "Scanning unavailable" }

        logger.info { message = "Starting scan" }
        val scan = bluetooth.requestLEScan(options).await()

        val listener: (Event) -> Unit = { event ->
            trySend(BluetoothAdvertisingEventWebBluetoothAdvertisement(event.unsafeCast<BluetoothAdvertisingEvent>())).getOrElse {
                logger.warn { message = "Unable to deliver advertisement event due to failure in flow or premature closing." }
            }
        }
        bluetooth.addEventListener(ADVERTISEMENT_RECEIVED_EVENT, listener)

        awaitClose {
            logger.info { message = "Stopping scan" }
            scan.stop()
            bluetooth.removeEventListener(ADVERTISEMENT_RECEIVED_EVENT, listener)
        }
    }
}
