package com.juul.kable

import com.benasher44.uuid.Uuid

/** https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth/requestDevice */
public fun Options(builder: OptionsBuilder.() -> Unit): Options =
    OptionsBuilder().apply(builder).build()

public data class Options internal constructor(
    internal val filters: List<FilterPredicate>,
    internal val optionalServices: List<Uuid>?,
)
