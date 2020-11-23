package com.juul.kable

public interface Central {

    // todo: Parameter of `List<Service>` to filter against for scanning.
    public fun scanner(): Scanner

    public fun peripheral(advertisement: Advertisement): Peripheral
}
