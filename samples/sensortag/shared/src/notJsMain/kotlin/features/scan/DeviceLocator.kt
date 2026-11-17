package com.juul.sensortag.features.scan

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope

actual fun ScreenModel.DeviceLocator(
    onRequestDeviceSuccess: suspend () -> Unit,
    onStatus: suspend (String?) -> Unit,
): DeviceLocator = ScannerDeviceLocator(screenModelScope, onStatus)
