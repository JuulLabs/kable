package com.juul.kable.server

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeServerEngine : ServerEngine {

    class Notification(
        val central: Central,
        val characteristic: ServerCharacteristic,
        val value: ByteArray,
    )

    /** Inbound requests, as if received from a remote central (via platform callbacks). */
    var requests = Channel<InboundRequest>(UNLIMITED)
        private set

    /** Notifications handed off to the (fake) platform. */
    val notifications = Channel<Notification>(UNLIMITED)

    val isOpen = MutableStateFlow(false)
    val advertising = MutableStateFlow<AdvertisementParameters?>(null)
    var openCount = 0
        private set

    override suspend fun open(profile: ServerProfile): ReceiveChannel<InboundRequest> {
        check(!isOpen.value) { "Already open" }
        openCount++
        isOpen.value = true
        requests = Channel(UNLIMITED)
        return requests
    }

    override suspend fun close() {
        isOpen.value = false
    }

    override suspend fun notify(central: Central, characteristic: ServerCharacteristic, value: ByteArray) {
        notifications.trySend(Notification(central, characteristic, value))
    }

    override suspend fun advertise(parameters: AdvertisementParameters): Nothing {
        advertising.value = parameters
        try {
            awaitCancellation()
        } finally {
            advertising.value = null
        }
    }
}
