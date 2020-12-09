package com.juul.kable

import com.juul.kable.external.Bluetooth
import com.juul.kable.external.BluetoothAdvertisingEvent
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.w3c.dom.events.Event

private const val ADVERTISEMENT_RECEIVED_EVENT = "advertisementreceived"

public actual fun scanner(): Scanner = JsScanner(bluetooth, Options())

/**
 * Only available on Chrome 79+ with "Experimental Web Platform features" enabled via:
 * chrome://flags/#enable-experimental-web-platform-features
 *
 * See also: [Chrome Platform Status: Web Bluetooth Scanning](https://www.chromestatus.com/feature/5346724402954240)
 */
public class JsScanner internal constructor(
    bluetooth: Bluetooth,
    options: Options
) : Scanner {

    // https://webbluetoothcg.github.io/web-bluetooth/scanning.html#scanning
    private val supportsScanning = js("window.navigator.bluetooth.requestLEScan") != null

    public override val advertisements: Flow<Advertisement> = callbackFlow {
        check(supportsScanning) { "Scanning unavailable" }

        val scan = bluetooth.requestLEScan(options.toDynamic()).await()
        val listener: (Event) -> Unit = {
            val event = it as BluetoothAdvertisingEvent
            offer(Advertisement(event.device, event.rssi))
        }
        bluetooth.addEventListener(ADVERTISEMENT_RECEIVED_EVENT, listener)

        awaitClose {
            scan.stop()
            bluetooth.removeEventListener(ADVERTISEMENT_RECEIVED_EVENT, listener)
        }
    }
}

// TODO: dedicated scan options class with the additional properties instead of re-using `Options`
// https://webbluetoothcg.github.io/web-bluetooth/scanning.html#dictdef-bluetoothlescanoptions
private fun Options.toDynamic(): dynamic = if (filters == null) {
    object {
        val acceptAllAdvertisements = true
        val keepRepeatedDevices = false
    }
} else {
    object {
        val keepRepeatedDevices = false
        val filters = this@toDynamic.filters
    }
}
