package com.juul.kable.bluetooth

import com.juul.kable.AssignedNumbers
import com.juul.kable.Bluetooth
import kotlin.uuid.toJavaUuid

internal val clientCharacteristicConfigUuid =
    (Bluetooth.BaseUuid + AssignedNumbers.Descriptors.CLIENT_CHARACTERISTIC_CONFIGURATION).toJavaUuid()
