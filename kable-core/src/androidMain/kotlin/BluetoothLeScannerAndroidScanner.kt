package com.juul.kable

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM
import android.os.ParcelUuid
import com.juul.kable.Filter.Address
import com.juul.kable.Filter.ManufacturerData
import com.juul.kable.Filter.Name
import com.juul.kable.Filter.Service
import com.juul.kable.Filter.ServiceData
import com.juul.kable.bluetooth.checkBluetoothIsOn
import com.juul.kable.logs.Logger
import com.juul.kable.logs.Logging
import com.juul.kable.scan.ScanError
import com.juul.kable.scan.message
import com.juul.kable.scan.requirements.checkLocationServicesEnabled
import com.juul.kable.scan.requirements.checkScanPermissions
import com.juul.kable.scan.requirements.requireBluetoothLeScanner
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlin.reflect.KClass
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal data class ScanFilters(

    /** [ScanFilter]s applied using Android's native filtering. */
    val native: List<ScanFilter>,

    /** [FilterPredicate]s applied via flow [filter][Flow.filter] operator. */
    val flow: List<FilterPredicate>,
)

internal class BluetoothLeScannerAndroidScanner(
    filters: List<FilterPredicate>,
    private val scanSettings: ScanSettings,
    private val preConflate: Boolean,
    logging: Logging,
) : PlatformScanner {

    private val logger = Logger(logging, tag = "Kable/Scanner", identifier = null)

    private val scanFilters = filters.toScanFilters()

    override val advertisements: Flow<PlatformAdvertisement> = callbackFlow {
        logger.debug { message = "Initializing scan" }
        val scanner = requireBluetoothLeScanner()

        // Permissions are checked early (fail-fast), as they cannot be unexpectedly revoked prior
        // to scanning (revoking permissions on Android restarts the app).
        logger.verbose { message = "Checking permissions for scanning" }
        checkScanPermissions()

        fun sendResult(scanResult: ScanResult) {
            val advertisement = ScanResultAndroidAdvertisement(scanResult)
            when {
                preConflate -> trySend(advertisement)
                else -> trySendBlocking(advertisement)
            }.onFailure {
                logger.warn { message = "Unable to deliver scan result due to failure in flow or premature closing." }
            }
        }

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                sendResult(result)
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                results.forEach(::sendResult)
            }

            override fun onScanFailed(errorCode: Int) {
                val scanError = ScanError(errorCode)
                logger.error {
                    detail("code", scanError.toString())
                    message = "Scan could not be started"
                }
                close(IllegalStateException(scanError.message))
            }
        }

        // These conditions could change prior to scanning, so we check them as close to
        // initiating the scan as feasible.
        logger.verbose { message = "Checking scanning requirements" }
        checkLocationServicesEnabled()
        checkBluetoothIsOn()

        logger.info {
            message = logMessage("Starting", preConflate, scanFilters)
        }
        scanner.startScan(scanFilters.native, scanSettings, callback)

        awaitClose {
            logger.info {
                message = logMessage("Stopping", preConflate, scanFilters)
            }
            // Can't check BLE state here, only Bluetooth, but should assume `IllegalStateException`
            // means BLE has been disabled.
            try {
                scanner.stopScan(callback)
            } catch (e: IllegalStateException) {
                logger.warn(e) { message = "Failed to stop scan. " }
            }
        }
    }.filter { advertisement ->
        if (scanFilters.flow.isEmpty()) {
            true
        } else {
            scanFilters.flow.matches(
                services = advertisement.uuids,
                name = advertisement.name,
                address = advertisement.address,
                manufacturerData = advertisement.manufacturerData,
                serviceData = advertisement.serviceData?.mapKeys { (key) -> key.uuid.toKotlinUuid() },
            )
        }
    }
}

private fun logMessage(
    prefix: String,
    preConflate: Boolean,
    scanFilters: ScanFilters,
) = buildString {
    append(prefix)
    append(' ')
    append("scan ")
    if (preConflate) {
        append("pre-conflated ")
    }
    if (scanFilters.native.isEmpty() && scanFilters.flow.isEmpty()) {
        append("without filters")
    } else {
        append("with ${scanFilters.native.count()} native and ${scanFilters.flow.count()} flow filter(s)")
    }
}

internal fun List<FilterPredicate>.toScanFilters(): ScanFilters =
    if (all(FilterPredicate::supportsNativeScanFiltering)) {
        ScanFilters(
            native = map(FilterPredicate::toNativeScanFilter),
            flow = emptyList(),
        )
    } else if (count() == 1) {
        val nativeFilters = mutableMapOf<KClass<*>, Filter>()
        val flowFilters = mutableListOf<Filter>()
        single().filters.forEach { filter ->
            if (filter.canFilterNatively && filter::class !in nativeFilters) {
                nativeFilters[filter::class] = filter
            } else {
                flowFilters += filter
            }
        }
        ScanFilters(
            native = listOf(nativeFilters.values.toList().toNativeScanFilter()),
            flow = listOf(FilterPredicate(flowFilters)),
        )
    } else {
        ScanFilters(
            native = emptyList(),
            flow = this,
        )
    }

// Android's `ScanFilter` does not support name prefix filtering, and only allows at most one of each filter type.
private val FilterPredicate.supportsNativeScanFiltering: Boolean
    get() {
        var nameExact = 0
        var service = 0
        var manufacturerData = 0
        var serviceData = 0
        var address = 0
        filters.forEach { filter ->
            when (filter) {
                is Name.Prefix -> return false
                is Name.Exact -> if (++nameExact > 1) return false
                is Address -> if (++address > 1) return false
                is Service -> if (++service > 1) return false
                is ManufacturerData -> if (++manufacturerData > 1) return false
                is ServiceData -> if (++serviceData > 1) return false
            }
        }
        return true
    }

private val Filter.canFilterNatively: Boolean
    get() = when (this) {
        is Name.Exact -> true
        is Address -> true
        is ManufacturerData -> true
        is ServiceData -> true
        is Service -> true
        else -> false
    }

private fun FilterPredicate.toNativeScanFilter(): ScanFilter = filters.toNativeScanFilter()

private fun List<Filter>.toNativeScanFilter(): ScanFilter =
    ScanFilter.Builder().apply {
        onEach { filter ->
            when (filter) {
                is Name.Exact -> setDeviceName(filter.exact)
                is Address -> setDeviceAddress(filter.address)
                is ManufacturerData -> setManufacturerData(filter.id, filterDataCompat(filter.data), filter.dataMask)
                is ServiceData -> setServiceData(ParcelUuid(filter.uuid.toJavaUuid()), filterDataCompat(filter.data), filter.dataMask)
                is Service -> setServiceUuid(ParcelUuid(filter.uuid.toJavaUuid()))
                else -> throw AssertionError("Unsupported filter element")
            }
        }
    }.build()

// Android doesn't properly check for nullness of manufacturer or service data until Android 16.
// See https://github.com/JuulLabs/kable/issues/854 for more details.
private fun filterDataCompat(data: ByteArray?): ByteArray? =
    if (data == null && SDK_INT <= VANILLA_ICE_CREAM) byteArrayOf() else data
