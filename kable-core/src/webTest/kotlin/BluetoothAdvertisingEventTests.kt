package com.juul.kable

import com.juul.kable.external.ADVERTISEMENT_RECEIVED
import com.juul.kable.external.addAdvertisementReceivedHandler
import web.events.Event
import web.events.EventTarget
import web.events.EventType
import kotlin.js.js
import kotlin.test.Test
import kotlin.test.assertEquals

class BluetoothAdvertisingEventTests {

    @Test
    fun listenerReceivesAdvertisementsUntilUnsubscribed() = withAdvertisementEvent(ADVERTISEMENT_RECEIVED) { event ->
        val target = EventTarget()
        var received = 0
        val unsubscribe = target.addAdvertisementReceivedHandler { received++ }

        target.dispatchEvent(event)
        target.dispatchEvent(event)
        assertEquals(2, received)

        unsubscribe()
        target.dispatchEvent(event)
        assertEquals(2, received)
    }

    @Test
    fun oneShotListenerReceivesOnlyTheFirstAdvertisement() = withAdvertisementEvent(ADVERTISEMENT_RECEIVED) { event ->
        val target = EventTarget()
        var received = 0
        target.addAdvertisementReceivedHandler(once = true) { received++ }

        target.dispatchEvent(event)
        target.dispatchEvent(event)
        assertEquals(1, received)
    }
}

private fun withAdvertisementEvent(type: EventType<Event>, test: (Event) -> Unit): Unit = js(
    """{
        const original = globalThis.BluetoothAdvertisingEvent;
        if (!original) globalThis.BluetoothAdvertisingEvent = class extends Event {};
        try {
            test(Object.setPrototypeOf(new Event(type), BluetoothAdvertisingEvent.prototype));
        } finally {
            if (!original) delete globalThis.BluetoothAdvertisingEvent;
        }
    }""",
)
