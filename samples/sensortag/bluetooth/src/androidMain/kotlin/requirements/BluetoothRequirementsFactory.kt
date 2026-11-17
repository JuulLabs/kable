package com.juul.sensortag.bluetooth.requirements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
public actual fun rememberBluetoothRequirementsFactory(): BluetoothRequirementsFactory {
    val applicationContext = LocalContext.current.applicationContext
    return remember(applicationContext) {
        BluetoothRequirementsFactory { AndroidBluetoothRequirements(applicationContext) }
    }
}
