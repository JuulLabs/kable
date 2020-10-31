package com.juul.kable

public interface Central {
    public fun scanner(): Scanner
    public fun peripheral(advertisement: Advertisement): Peripheral
}
