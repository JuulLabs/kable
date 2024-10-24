package com.juul.sensortag.bluetooth

import androidx.compose.runtime.Composable

public interface SystemControl {
    public fun showLocationSettings()
    public fun requestToTurnBluetoothOn()
}

@Composable
public expect fun rememberSystemControl(): SystemControl
