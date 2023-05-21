package com.juul.kable

@Deprecated(
    message = "Moved as nested class of `AndroidAdvertisement`.",
    replaceWith = ReplaceWith("AndroidAdvertisement.BondState"),
)
public typealias BondState = AndroidAdvertisement.BondState

public interface AndroidAdvertisement : Advertisement {

    public enum class BondState {
        None,
        Bonding,
        Bonded,
    }

    public val address: String
    public val bondState: BondState
    public val bytes: ByteArray?
}
