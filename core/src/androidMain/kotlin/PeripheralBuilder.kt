package com.juul.kable

/** Preferred transport for GATT connections to remote dual-mode devices. */
public enum class Transport {

    /** No preference of physical transport for GATT connections to remote dual-mode devices. */
    Auto,

    /** Prefer BR/EDR transport for GATT connections to remote dual-mode devices. */
    BrEdr,

    /** Prefer LE transport for GATT connections to remote dual-mode devices. */
    Le,
}

/** Preferred Physical Layer (PHY) for connections to remote LE devices. */
public enum class Phy {

    /** Bluetooth LE 1M PHY. */
    Le1M,

    /**
     * Bluetooth LE 2M PHY.
     *
     * Per [Exploring Bluetooth 5 – Going the Distance](https://www.bluetooth.com/blog/exploring-bluetooth-5-going-the-distance/#mcetoc_1d7vdh6b25):
     * "The new LE 2M PHY allows the physical layer to operate at 2 Ms/s and thus enables higher data rates than LE 1M
     * and Bluetooth 4."
     */
    Le2M,

    /**
     * Bluetooth LE Coded PHY.
     *
     * Per [Exploring Bluetooth 5 – Going the Distance](https://www.bluetooth.com/blog/exploring-bluetooth-5-going-the-distance/#mcetoc_1d7vdh6b26):
     * "The LE Coded PHY allows range to be quadrupled (approximately), compared to Bluetooth® 4 and this has been
     * accomplished without increasing the transmission power required."
     */
    LeCoded,
}

public actual class PeripheralBuilder internal actual constructor() {

    public actual var onConnect: OnConnectAction = {}

    /** Preferred transport for GATT connections to remote dual-mode devices. */
    public var transport: Transport = Transport.Le

    /** Preferred PHY for connections to remote LE device. */
    public var phy: Phy = Phy.Le1M
}
