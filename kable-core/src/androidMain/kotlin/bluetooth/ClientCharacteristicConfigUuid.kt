package com.juul.kable.bluetooth

import com.juul.kable.Bluetooth
import kotlin.uuid.toJavaUuid

internal val clientCharacteristicConfigUuid = (Bluetooth.BaseUuid + 0x2902).toJavaUuid()
