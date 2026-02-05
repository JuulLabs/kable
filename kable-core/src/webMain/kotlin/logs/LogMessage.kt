package com.juul.kable.logs

import com.juul.kable.external.BluetoothRemoteGATTCharacteristic
import com.juul.kable.logs.Logging.DataProcessor.Operation
import com.juul.kable.toByteArray
import org.khronos.webgl.DataView

internal actual val LOG_INDENT: String? = "  "

internal fun LogMessage.detail(data: DataView?, operation: Operation) {
    detail(data?.buffer?.toByteArray(), operation)
}

internal fun LogMessage.detail(characteristic: BluetoothRemoteGATTCharacteristic) {
    detail("service", characteristic.service.uuid)
    detail("characteristic", characteristic.uuid)
}
