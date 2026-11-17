package com.juul.sensortag.features.components

import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import com.juul.sensortag.icons.BluetoothDisabled

@Composable
internal fun BluetoothDisabled(enableAction: () -> Unit) {
    ActionRequired(
        icon = Icons.Filled.BluetoothDisabled,
        contentDescription = "Bluetooth disabled",
        description = "Bluetooth is disabled.",
        buttonText = "Enable",
        onClick = enableAction,
    )
}
