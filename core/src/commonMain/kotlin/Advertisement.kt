package com.juul.kable

import com.benasher44.uuid.Uuid

public interface Advertisement {

    /**
     * The name in the Advertisement.
     *
     * The data source for this property changed in 0.21.0, for the previous (often cached) data source for the name,
     * use [peripheralName].
     */
    public val name: String?

    /**
     * It is recommended that [name] be used instead, as the backing value for [peripheralName] may differ between the
     * various platforms.
     *
     * On Apple, this may be a shortened version of the peripheral name.
     *
     * On most platforms, this will return the cached peripheral name, but the caching strategy differs per platform.
     */
    public val peripheralName: String?

    /**
     * Platform specific identifier for the remote peripheral. On some platforms, this can be used
     * to "restore" a previously known peripheral for reconnection.
     *
     * On Android, this is a MAC address represented as a [String]. A [Peripheral] can be created
     * from this MAC address using the `CoroutineScope.peripheral(String, PeripheralBuilderAction)`
     * function unless the peripheral makes "use of a Bluetooth Smart feature known as 'LE Privacy'"
     * (whereas the peripheral may provide a random MAC address, see
     * [Bluetooth Technology Protecting Your Privacy](https://www.bluetooth.com/blog/bluetooth-technology-protecting-your-privacy/)
     * for more details)).
     *
     * On Apple, this is a unique identifier represented as a [Uuid]. A [Peripheral] can be created
     * from this identifier using the `CoroutineScope.peripheral(Uuid, PeripheralBuilderAction)`
     * function. According to
     * [The Ultimate Guide to Apple’s Core Bluetooth](https://punchthrough.com/core-bluetooth-basics/):
     *
     * > This UUID isn't guaranteed to stay the same across scanning sessions and should not be 100%
     * > relied upon for peripheral re-identification. That said, we have observed it to be
     * > relatively stable and reliable over the long term assuming a major device settings reset
     * > has not occurred.
     *
     * If `CoroutineScope.peripheral(Uuid, PeripheralBuilderAction)` throws a
     * [NoSuchElementException] then a scan will be necessary to obtain an [Advertisement] for
     * [Peripheral] creation.
     *
     * On JavaScript, this is a unique identifier represented as a [String]. "Restoring" a
     * peripheral from this identifier is not yet supported in Kable (as JavaScript requires user to
     * [explicitly enable this feature](https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth/getDevices)).
     */
    public val identifier: Identifier

    /** Returns if the peripheral is connectable. */
    public val isConnectable: Boolean?

    /**
     * The received signal strength, in dBm, of the packet received.
     *
     * On JavaScript platform, returns `Int.MIN_VALUE` when RSSI is unavailable.
     */
    public val rssi: Int

    /**
     * The TX Power Level data type indicates the transmitted/radiated power level of the Advertisement packet.
     * The path loss on a received packet may be calculated using the following equation:
     * `pathloss = txPower – rssi`
     */
    public val txPower: Int?

    /**
     * A list of Service or Service Class UUIDs.
     * According to the BLE specification, GAP and GATT service UUIDs should not be included here.
     */
    public val uuids: List<Uuid>

    /**
     * Lookup the data associated with a Service
     *
     * @param uuid the Service UUID
     * @return the data associated with the service or `null` if not found
     */
    public fun serviceData(uuid: Uuid): ByteArray?

    /**
     * Lookup the Manufacturer Specific Data by
     * [Company Identifier Code][https://www.bluetooth.com/specifications/assigned-numbers/company-identifiers/]
     *
     * @param companyIdentifierCode the two-octet code identifying the manufacturer
     * @return the Manufacturer Data for the given code (does not include the leading two identifier octets),
     * or `null` if not found
     */
    public fun manufacturerData(companyIdentifierCode: Int): ByteArray?

    /**
     * The Manufacturer Specific Data, or null if none provided in the Advertisement packet.
     */
    public val manufacturerData: ManufacturerData?
}
