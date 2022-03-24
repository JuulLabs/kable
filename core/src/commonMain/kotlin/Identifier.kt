package com.juul.kable

public expect class Identifier

/**
 * The identifier of the remote device
 *
 * This identifier is the identifier returned by the operating system for the address of the device.
 * The value of the identifier will differ between operating system implementations.  On iOS
 * the identifier is a UUID.  On Android the identifier is a MAC address.  For security reasons
 * the identifier is not a MAC address on iOS and may be a randomized MAC on Android.  The identifier
 * may also change.
 */
public expect val Peripheral.identifier: Identifier

public expect fun String.toIdentifier(): Identifier
