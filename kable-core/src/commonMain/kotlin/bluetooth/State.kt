package com.juul.kable.bluetooth

import com.juul.kable.Bluetooth
import kotlinx.coroutines.flow.Flow

internal expect val state: Flow<Bluetooth.State>
