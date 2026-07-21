package com.juul.kable.server

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_BALANCED
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_LOW_POWER
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_LOW
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW
import android.os.ParcelUuid
import kotlin.time.Duration
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/** Advertise mode (matching [AdvertiseSettings] advertise modes). */
public enum class AdvertiseMode {

    /** Advertise in low power mode (default; least battery usage, largest advertising interval). */
    LowPower,

    /** Advertise in balanced power mode. */
    Balanced,

    /** Advertise in low latency mode (most battery usage, smallest advertising interval). */
    LowLatency,
}

/** Advertise transmission (TX) power level (matching [AdvertiseSettings] TX power levels). */
public enum class AdvertiseTxPower {
    UltraLow,
    Low,
    Medium,
    High,
}

internal actual class AdvertisementParameters(
    val name: String?,
    val services: List<Uuid>,
    val connectable: Boolean,
    val timeout: Duration,
    val mode: AdvertiseMode,
    val txPower: AdvertiseTxPower,
    val includeTxPowerLevel: Boolean,
    val manufacturerData: Map<Int, ByteArray>,
    val serviceData: Map<Uuid, ByteArray>,
)

public actual class AdvertisementParametersBuilder internal actual constructor() {

    public actual var name: String? = null
    public actual var services: List<Uuid> = emptyList()

    /** Whether the advertisement is connectable. */
    public var connectable: Boolean = true

    /**
     * Advertising duration limit, or [Duration.ZERO] (default) to advertise until
     * [advertising][GattServer.advertise] is cancelled. Must not exceed 180 seconds (per Android
     * limit).
     */
    public var timeout: Duration = Duration.ZERO

    public var mode: AdvertiseMode = AdvertiseMode.LowPower

    public var txPower: AdvertiseTxPower = AdvertiseTxPower.Medium

    /** Whether to include the transmission (TX) power level in the advertisement. */
    public var includeTxPowerLevel: Boolean = false

    private val manufacturerData = mutableMapOf<Int, ByteArray>()
    private val serviceData = mutableMapOf<Uuid, ByteArray>()

    /** Adds manufacturer specific [data] (for manufacturer [companyId]) to the advertisement. */
    public fun manufacturerData(companyId: Int, data: ByteArray) {
        manufacturerData[companyId] = data.copyOf()
    }

    /** Adds service [data] (for the service identified by [uuid]) to the advertisement. */
    public fun serviceData(uuid: Uuid, data: ByteArray) {
        serviceData[uuid] = data.copyOf()
    }

    internal actual fun build(): AdvertisementParameters {
        require(timeout >= Duration.ZERO && timeout.inWholeMilliseconds <= 180_000) {
            "timeout must be between 0 and 180 seconds, was $timeout"
        }
        return AdvertisementParameters(
            name = name,
            services = services.toList(),
            connectable = connectable,
            timeout = timeout,
            mode = mode,
            txPower = txPower,
            includeTxPowerLevel = includeTxPowerLevel,
            manufacturerData = manufacturerData.toMap(),
            serviceData = serviceData.toMap(),
        )
    }
}

internal fun AdvertisementParameters.toAdvertiseSettings(): AdvertiseSettings = AdvertiseSettings.Builder()
    .setConnectable(connectable)
    .setTimeout(timeout.inWholeMilliseconds.toInt())
    .setAdvertiseMode(
        when (mode) {
            AdvertiseMode.LowPower -> ADVERTISE_MODE_LOW_POWER
            AdvertiseMode.Balanced -> ADVERTISE_MODE_BALANCED
            AdvertiseMode.LowLatency -> ADVERTISE_MODE_LOW_LATENCY
        },
    )
    .setTxPowerLevel(
        when (txPower) {
            AdvertiseTxPower.UltraLow -> ADVERTISE_TX_POWER_ULTRA_LOW
            AdvertiseTxPower.Low -> ADVERTISE_TX_POWER_LOW
            AdvertiseTxPower.Medium -> ADVERTISE_TX_POWER_MEDIUM
            AdvertiseTxPower.High -> ADVERTISE_TX_POWER_HIGH
        },
    )
    .build()

internal fun AdvertisementParameters.toAdvertiseData(): AdvertiseData = AdvertiseData.Builder()
    .setIncludeDeviceName(name != null)
    .setIncludeTxPowerLevel(includeTxPowerLevel)
    .apply {
        services.forEach { addServiceUuid(ParcelUuid(it.toJavaUuid())) }
        manufacturerData.forEach { (companyId, data) -> addManufacturerData(companyId, data) }
        serviceData.forEach { (uuid, data) -> addServiceData(ParcelUuid(uuid.toJavaUuid()), data) }
    }
    .build()
