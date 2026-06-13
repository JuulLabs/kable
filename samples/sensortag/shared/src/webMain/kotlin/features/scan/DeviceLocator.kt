package com.juul.sensortag.features.scan

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.browser.window

actual fun ScreenModel.DeviceLocator(
    onRequestDeviceSuccess: suspend () -> Unit,
    onStatus: suspend (String?) -> Unit,
): DeviceLocator = if (browserSupportsScanning) {
    ScannerDeviceLocator(screenModelScope, onStatus)
} else {
    RequestDeviceLocator(screenModelScope, onRequestDeviceSuccess, onStatus)
}

private val browserSupportsScanning: Boolean
    // https://github.com/JuulLabs/topaz supports scanning.
    get() = window.navigator.userAgent.contains("topaz", ignoreCase = true)
