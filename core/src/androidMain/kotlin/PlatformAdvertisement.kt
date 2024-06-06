package com.juul.kable

import android.os.Parcelable

@Deprecated(
    message = "Moved as nested class of `PlatformAdvertisement`.",
    replaceWith = ReplaceWith("PlatformAdvertisement.BondState"),
    level = DeprecationLevel.ERROR,
)
public typealias BondState = PlatformAdvertisement.BondState

public actual interface PlatformAdvertisement : Advertisement, Parcelable {

    public enum class BondState {
        None,
        Bonding,
        Bonded,
    }

    public val address: String
    public val bondState: BondState
    public val bytes: ByteArray?
}
