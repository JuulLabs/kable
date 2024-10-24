package com.juul.sensortag.features.scan

import com.juul.kable.Options
import com.juul.kable.PlatformAdvertisement
import com.juul.kable.logs.Logging.Level.Data
import com.juul.kable.requestPeripheral
import com.juul.sensortag.SensorTag
import com.juul.sensortag.features.scan.DeviceLocator.State.NotYetScanned
import com.juul.sensortag.peripheral
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private val options = Options {
    filters {
        match {
            services = listOf(SensorTag.Uuid)
        }
    }
    optionalServices = SensorTag.services
}

class RequestDeviceLocator(
    private val parentScope: CoroutineScope,
    private val onRequestDeviceSuccess: suspend () -> Unit,
    private val onStatus: suspend (String?) -> Unit,
) : DeviceLocator {

    override val state: StateFlow<DeviceLocator.State> = MutableStateFlow(NotYetScanned)
    override val advertisements: StateFlow<List<PlatformAdvertisement>> = MutableStateFlow(emptyList())

    override fun run() {
        parentScope.launch {
            onStatus(null)
            try {
                peripheral = requestPeripheral(options) {
                    logging { level = Data }
                } ?: return@launch
                onRequestDeviceSuccess()
            } catch (e: Exception) {
                onStatus(e.message ?: "Unknown error")
            }
        }
    }

    override suspend fun cancelAndJoin() {
        // No-op
    }

    override suspend fun clear() {
        // No-op
    }
}
