package com.juul.kable.server

import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED
import android.bluetooth.le.AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE
import android.bluetooth.le.AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED
import android.bluetooth.le.AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR
import android.bluetooth.le.AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import com.juul.kable.server.logs.Logger
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlin.coroutines.resumeWithException
import kotlin.uuid.toKotlinUuid

internal class AndroidServerEngine(
    private val logger: Logger,
) : ServerEngine {

    private val bluetoothManager: BluetoothManager
        get() = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
            ?: error("BluetoothManager not available")

    // Guarded by `guard`.
    private var callback: ServerCallback? = null
    private var server: BluetoothGattServer? = null
    private var characteristics = emptyMap<AttributeKey.Characteristic, BluetoothGattCharacteristic>()

    private val guard = Mutex()

    /** Serializes notifications: Android only supports one in-flight notification at a time. */
    private val notifying = Mutex()

    override suspend fun open(profile: ServerProfile): ReceiveChannel<InboundRequest> = guard.withLock {
        check(server == null) { "GATT server already open" }

        val callback = ServerCallback(logger)
        val server = bluetoothManager.openGattServer(applicationContext, callback)
            ?: error("Unable to open GATT server (is Bluetooth supported and enabled?)")
        callback.server = server
        this.callback = callback
        this.server = server

        try {
            val characteristics = mutableMapOf<AttributeKey.Characteristic, BluetoothGattCharacteristic>()
            profile.services.forEach { service ->
                val platformService = service.toBluetoothGattService()
                platformService.characteristics.forEach { characteristic ->
                    characteristics[
                        AttributeKey.Characteristic(
                            serviceUuid = service.uuid,
                            characteristicUuid = characteristic.uuid.toKotlinUuid(),
                        ),
                    ] = characteristic
                }

                // `addService` must not be called again until `onServiceAdded` is received.
                check(server.addService(platformService)) { "addService request rejected for service ${service.uuid}" }
                val status = callback.onServiceAdded.receive()
                check(status == GATT_SUCCESS) { "Failed to add service ${service.uuid}, status: ${status.gattStatusString}" }
            }
            this.characteristics = characteristics.toMap()
        } catch (e: Exception) {
            closeServer()
            throw e
        }

        callback.requests
    }

    override suspend fun close(): Unit = guard.withLock {
        closeServer()
    }

    private fun closeServer() {
        callback?.close()
        server?.apply {
            clearServices()
            close()
        }
        callback = null
        server = null
        characteristics = emptyMap()
    }

    override suspend fun notify(central: Central, characteristic: ServerCharacteristic, value: ByteArray) {
        val device = (central as AndroidCentral).device
        val key = AttributeKey.Characteristic(characteristic.serviceUuid, characteristic.characteristicUuid)
        val confirm = characteristic.subscription?.indication == true

        // `server`/`callback`/`characteristics` are read without `guard` (which would deadlock
        // `notify` against `close`, as `close` cancels-and-joins notifying coroutines); failures
        // due to closure racing `notify` manifest as (caught or propagated) `IOException`s.
        val server = checkNotNull(server) { "GATT server not open" }
        val callback = checkNotNull(callback) { "GATT server not open" }
        val platformCharacteristic = characteristics.getValue(key)

        notifying.withLock {
            logger.debug {
                "notifyCharacteristicChanged address=${device.address} " +
                    "uuid=${characteristic.characteristicUuid} confirm=$confirm" + logger.data(value)
            }
            if (SDK_INT >= 33) {
                val status = server.notifyCharacteristicChanged(device, platformCharacteristic, confirm, value)
                if (status != BluetoothStatusCodes.SUCCESS) {
                    throw IOException("notifyCharacteristicChanged failed with status $status")
                }
            } else {
                @Suppress("DEPRECATION")
                platformCharacteristic.value = value
                @Suppress("DEPRECATION")
                if (!server.notifyCharacteristicChanged(device, platformCharacteristic, confirm)) {
                    throw IOException("notifyCharacteristicChanged failed")
                }
            }
            val status = callback.onNotificationSent.receive()
            if (status != GATT_SUCCESS) {
                throw IOException("Notification failed with status ${status.gattStatusString}")
            }
        }
    }

    override suspend fun advertise(parameters: AdvertisementParameters): Nothing {
        val adapter = bluetoothManager.adapter ?: throw AdvertiseException("Bluetooth adapter not available")
        val advertiser = adapter.bluetoothLeAdvertiser
            ?: throw AdvertiseException("Bluetooth LE advertising not available (is Bluetooth enabled?)")

        if (parameters.name != null && parameters.name != adapter.name) {
            logger.warn {
                "Requested advertised name '${parameters.name}' differs from Bluetooth adapter name " +
                    "'${adapter.name}'; the adapter name will be advertised (Android can only advertise " +
                    "the adapter name — use `BluetoothAdapter.setName` to change it)"
            }
        }

        return suspendCancellableCoroutine<Nothing> { continuation ->
            val callback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    logger.debug { "Advertising started: $settingsInEffect" }
                    // Deliberately does not resume `continuation`: advertising remains active (and
                    // this function remains suspended) until cancelled.
                }

                override fun onStartFailure(errorCode: Int) {
                    continuation.resumeWithException(
                        AdvertiseException("Failed to start advertising: ${errorCode.advertiseFailureString}"),
                    )
                }
            }
            advertiser.startAdvertising(parameters.toAdvertiseSettings(), parameters.toAdvertiseData(), callback)
            continuation.invokeOnCancellation {
                logger.debug { "Advertising stopped" }
                advertiser.stopAdvertising(callback)
            }
        }
    }
}

private val Int.gattStatusString: String
    get() = "GATT_STATUS_$this"

private val Int.advertiseFailureString: String
    get() = when (this) {
        ADVERTISE_FAILED_DATA_TOO_LARGE -> "ADVERTISE_FAILED_DATA_TOO_LARGE"
        ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS"
        ADVERTISE_FAILED_ALREADY_STARTED -> "ADVERTISE_FAILED_ALREADY_STARTED"
        ADVERTISE_FAILED_INTERNAL_ERROR -> "ADVERTISE_FAILED_INTERNAL_ERROR"
        ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "ADVERTISE_FAILED_FEATURE_UNSUPPORTED"
        else -> "Unknown($this)"
    }
