package com.juul.sensortag.features.scan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.Advertisement
import com.juul.sensortag.central
import com.juul.sensortag.features.scan.ScanStatus.Failed
import com.juul.sensortag.features.scan.ScanStatus.Started
import com.juul.sensortag.features.scan.ScanStatus.Stopped
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit

private val SCAN_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(10)

sealed class ScanStatus {
    object Stopped : ScanStatus()
    object Started : ScanStatus()
    data class Failed(val message: CharSequence) : ScanStatus()
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val scanner = central.scanner()
    private val scanScope = viewModelScope.childScope()
    private val found = hashMapOf<String, Advertisement>()

    private val _scanStatus = MutableStateFlow<ScanStatus>(Stopped)
    val scanStatus = _scanStatus.asStateFlow()

    private val _advertisements = MutableStateFlow<List<Advertisement>>(emptyList())
    val advertisements = _advertisements.asStateFlow()

    fun startScan() {
        if (_scanStatus.value == Started) return // Scan already in progress.
        _scanStatus.value = Started

        scanScope.launch {
            withTimeoutOrNull(SCAN_DURATION_MILLIS) {
                scanner.peripherals
                    .catch { cause -> _scanStatus.value = Failed(cause.message ?: "Unknown error") }
                    .onCompletion { cause -> if (cause == null) _scanStatus.value = Stopped }
                    .filter { it.isSensorTag }
                    .collect { advertisement ->
                        found[advertisement.address] = advertisement
                        _advertisements.value = found.values.toList()
                    }
            }
        }
    }

    fun stopScan() {
        scanScope.cancelChildren()
    }
}

private val Advertisement.isSensorTag
    get() = name?.startsWith("SensorTag") == true ||
        name?.startsWith("CC2650 SensorTag") == true

private fun CoroutineScope.childScope() =
    CoroutineScope(coroutineContext + Job(coroutineContext[Job]))

private fun CoroutineScope.cancelChildren(
    cause: CancellationException? = null
) = coroutineContext[Job]?.cancelChildren(cause)
