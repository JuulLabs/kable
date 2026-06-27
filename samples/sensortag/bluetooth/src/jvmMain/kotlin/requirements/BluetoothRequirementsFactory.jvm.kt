package com.juul.sensortag.bluetooth.requirements

import androidx.compose.runtime.Composable

@Composable
public actual fun rememberBluetoothRequirementsFactory(): BluetoothRequirementsFactory =
    BluetoothRequirementsFactory { NopBluetoothRequirements }
