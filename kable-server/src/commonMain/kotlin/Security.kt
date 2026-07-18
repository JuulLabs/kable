package com.juul.kable.server

import com.juul.kable.ExperimentalKableApi

/** Security required for a remote [Central] to perform an operation against an attribute. */
@ExperimentalKableApi
public enum class Security {

    /** No security required. */
    None,

    /** An encrypted link (pairing) is required. */
    Encrypted,

    /**
     * An encrypted link with man-in-the-middle protection (authenticated pairing) is required.
     *
     * On Apple, Core Bluetooth does not distinguish man-in-the-middle protection; treated the same
     * as [Encrypted].
     */
    EncryptedMitm,
}
