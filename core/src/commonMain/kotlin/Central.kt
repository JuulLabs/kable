package com.juul.kable

import com.benasher44.uuid.Uuid

public expect class Central {

    public fun scanner(
        services: List<Uuid>? = null,
    ): Scanner

    public fun peripheral(
        advertisement: Advertisement,
    ): Peripheral
}
