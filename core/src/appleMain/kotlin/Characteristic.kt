package com.juul.kable

import com.benasher44.uuid.Uuid

public actual interface Characteristic {
    public actual val serviceUuid: Uuid
    public actual val characteristicUuid: Uuid
}
