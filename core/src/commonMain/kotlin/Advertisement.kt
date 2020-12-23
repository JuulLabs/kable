package com.juul.kable

import com.benasher44.uuid.Uuid

public expect class Advertisement {
    /**
     * The Local Name data type shall be the same as, or a shortened version of, the local name assigned to the device
     */
    public val name: String?

    /**
     * The received signal strength, in dBm, of the packet received
     */
    public val rssi: Int

    /**
     * The TX Power Level data type indicates the transmitted/radiated power level of the packet.
     * The path loss on a received packet may be calculated using the following equation:
     * `pathloss = txPower – rssi`
     */
    public val txPower: Int?

    /**
     * The Service UUID data type is used to include a list of Service or Service Class UUIDs
     * GAP and GATT service UUIDs should not be included in a Service UUIDs AD type
     */
    public val uuids: List<Uuid>

    /**
     * The Service Data data type consists of a service UUID with the data associated with that service
     */
    public fun serviceData(uuid: Uuid): ByteArray?

    /**
     * The Manufacturer Specific data type is used for manufacturer specific data
     * Company Identifier Codes: https://www.bluetooth.com/specifications/assigned-numbers/company-identifiers/
     */
    public fun manufacturerData(companyIdentifierCode: Short): ByteArray?
}
