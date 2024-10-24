package com.juul.sensortag.bluetooth.requirements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
public actual fun rememberBluetoothRequirementsFactory(): BluetoothRequirementsFactory =
    remember { BluetoothRequirementsFactory { AppleBluetoothRequirements } }
