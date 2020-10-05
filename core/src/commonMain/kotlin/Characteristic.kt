package com.juul.kable

import com.benasher44.uuid.Uuid

public enum class WriteType {
    WithResponse,
    WithoutResponse,
}

public expect class Characteristic {
    public val uuid: Uuid
    public val descriptors: List<Descriptor>
}
