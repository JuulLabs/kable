package com.juul.kable

import com.benasher44.uuid.Uuid

public expect class Advertisement {
    /**
     * The name in the Advertisement.
     * The same as, or a shortened version of, the local name assigned to the device
     */
    public val name: String?

    /**
     * The received signal strength, in dBm, of the packet received
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
     * @param uuid the Service UUID
     * @return the data associated with the service or null if not found
     */
    public fun serviceData(uuid: Uuid): ByteArray?

    /**
     * Lookup the Manufacturer Specific Data by
     * [Company Identifier Code][https://www.bluetooth.com/specifications/assigned-numbers/company-identifiers/]
     * @param companyIdentifierCode the two-octet code identifying the manufacturer
     * @return the Manufacturer Data for the given code (does not include the leading two identifier octets)
     */
    public fun manufacturerData(companyIdentifierCode: Short): ByteArray?

    /**
     * The Manufacturer Specific Data, or null if none provided in the Advertisement packet.
     */
    public val manufacturerData: ManufacturerData?
}
