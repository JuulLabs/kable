package com.juul.sensortag.features.scan

import com.juul.kable.PlatformAdvertisement

sealed class ViewState {
    data object Unsupported : ViewState()
    data object Scan : ViewState()
    data object PermissionDenied : ViewState()
    data object LocationServicesDisabled : ViewState()
    data object BluetoothOff : ViewState()
    data class Devices(
        val scanState: DeviceLocator.State,
        val advertisements: List<PlatformAdvertisement>,
    ) : ViewState()
}
