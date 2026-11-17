package com.juul.sensortag.features.scan

import cafe.adriel.voyager.core.model.ScreenModel
import com.juul.kable.PlatformAdvertisement
import kotlinx.coroutines.flow.StateFlow

interface DeviceLocator {

    enum class State {
        NotYetScanned,
        Scanning,
        Scanned,
    }

    /** On Javascript, value is always [State.NotYetScanned]. */
    val state: StateFlow<State>

    /** Value is always an empty [List] on JavaScript. */
    val advertisements: StateFlow<List<PlatformAdvertisement>>

    fun run()

    /** No-op on Javascript. */
    suspend fun cancelAndJoin()

    /** No-op on Javascript. */
    suspend fun clear()
}

expect fun ScreenModel.DeviceLocator(
    /** JavaScript only. */
    onRequestDeviceSuccess: suspend () -> Unit,

    onStatus: suspend (String?) -> Unit,
): DeviceLocator
