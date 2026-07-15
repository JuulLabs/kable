package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.serialization.Serializable

@Serializable(with = PlatformAdvertisementSerializer::class)
public actual interface PlatformAdvertisement : Advertisement, Parcelable {

    public enum class BondState {
        None,
        Bonding,
        Bonded,
    }

    public val address: String
    public val bondState: BondState
    public val bytes: ByteArray?

    /**
     * This is an internal API and may be removed from a future release. If you are using it, please
     * open an issue and report your use case.
     */
    @InternalKableApi
    public val bluetoothDevice: BluetoothDevice
}
