package com.juul.kable

public enum class BondState {
    None,
    Bonding,
    Bonded,
}

public interface AndroidAdvertisement : Advertisement {
    public val address: String
    public val bondState: BondState
    public val bytes: ByteArray?
}
