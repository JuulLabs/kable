package com.juul.kable.server

import com.juul.kable.ExperimentalKableApi

/**
 * Attribute Protocol (ATT) error codes, used to reject requests from remote [centrals][Central]
 * (by throwing [GattErrorException] from a [ReadAction] or [WriteAction]).
 *
 * [Bluetooth Core Specification, Vol 3, Part F: 3.4.1.1 Error Response](https://www.bluetooth.com/specifications/specs/?types=adopted&keyword=Core+Specification)
 */
@ExperimentalKableApi
public enum class AttError(public val code: Int) {
    InvalidHandle(0x01),
    ReadNotPermitted(0x02),
    WriteNotPermitted(0x03),
    InvalidPdu(0x04),
    InsufficientAuthentication(0x05),
    RequestNotSupported(0x06),
    InvalidOffset(0x07),
    InsufficientAuthorization(0x08),
    PrepareQueueFull(0x09),
    AttributeNotFound(0x0A),
    AttributeNotLong(0x0B),
    InsufficientEncryptionKeySize(0x0C),
    InvalidAttributeValueLength(0x0D),
    UnlikelyError(0x0E),
    InsufficientEncryption(0x0F),
    UnsupportedGroupType(0x10),
    InsufficientResources(0x11),
}
