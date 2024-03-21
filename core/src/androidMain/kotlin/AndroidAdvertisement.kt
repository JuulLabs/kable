package com.juul.kable

import android.os.Parcelable

@Deprecated(
    message = "Moved as nested class of `AndroidAdvertisement`.",
    replaceWith = ReplaceWith("AndroidAdvertisement.BondState"),
)
public typealias BondState = AndroidAdvertisement.BondState

public interface AndroidAdvertisement : Advertisement, Parcelable {

    public enum class BondState {
        None,
        Bonding,
        Bonded,
    }

    public val address: String
    public val bondState: BondState
    public val bytes: ByteArray?
}
