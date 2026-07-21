package com.juul.kable.server

import kotlin.uuid.Uuid

/** Platform specific advertisement parameters (configured via [AdvertisementParametersBuilder]). */
internal expect class AdvertisementParameters

/**
 * Configures the data (and behavior) of an [advertisement][GattServer.advertise].
 *
 * Additional platform specific parameters are available on the platform specific builders.
 */
public expect class AdvertisementParametersBuilder internal constructor() {

    /**
     * Local name to advertise, or `null` (default) to not advertise a name.
     *
     * Platform specific behavior:
     *
     * - Android: The advertisement carries the Bluetooth adapter's name (device name) — it is not
     *   possible to advertise a custom name without changing the (system-wide) adapter name; when
     *   [name] is non-`null`, the device name is included in the advertisement (a warning is logged
     *   if [name] differs from the adapter name — to advertise [name] verbatim, the application
     *   must set `BluetoothAdapter.setName` itself).
     * - Apple: Advertised as the local name (only while the application is in the foreground).
     */
    public var name: String?

    /**
     * Service UUIDs to advertise.
     *
     * Advertisement space is limited (e.g. 28 bytes on Apple while the app is in the foreground);
     * limit the advertised service UUIDs to those that identify primary services (service UUIDs
     * that do not fit may be dropped, or on Apple, moved to a special "overflow" area only
     * discoverable by other Apple devices).
     */
    public var services: List<Uuid>

    internal fun build(): AdvertisementParameters
}
