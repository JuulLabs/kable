package com.juul.sensortag.bluetooth.requirements

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal object NopBluetoothRequirements : BluetoothRequirements {
    override val deficiencies: Flow<Set<Deficiency>> = flowOf(emptySet())
}
