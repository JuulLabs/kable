package com.juul.kable

import com.benasher44.uuid.Uuid

public expect class Service {
    public val uuid: Uuid
    public val characteristics: List<Characteristic>
}
