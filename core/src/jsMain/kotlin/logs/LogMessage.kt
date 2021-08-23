package com.juul.kable.logs

import com.juul.kable.external.BluetoothRemoteGATTCharacteristic
import com.juul.kable.external.BluetoothRemoteGATTService
import com.juul.kable.toByteArray
import org.khronos.webgl.DataView

internal actual val LOG_INDENT: String? = "  "

internal fun LogMessage.detail(data: DataView?) {
    if (data != null) detail(data.buffer.toByteArray())
}

internal fun LogMessage.detail(service: BluetoothRemoteGATTService) {
    detail("service", service.uuid)
}

internal fun LogMessage.detail(characteristic: BluetoothRemoteGATTCharacteristic) {
    detail(characteristic.service)
    detail("characteristic", characteristic.uuid)
}
