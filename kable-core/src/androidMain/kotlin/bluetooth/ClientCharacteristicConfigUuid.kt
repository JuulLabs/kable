package com.juul.kable.bluetooth

import com.juul.kable.external.CLIENT_CHARACTERISTIC_CONFIG_UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal val clientCharacteristicConfigUuid = Uuid.parse(CLIENT_CHARACTERISTIC_CONFIG_UUID)
