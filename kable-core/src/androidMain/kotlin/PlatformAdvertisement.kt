package com.juul.kable

import android.os.Parcelable

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
