package com.juul.sensortag.features.scan

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import com.juul.kable.Bluetooth
import com.juul.kable.ExperimentalApi
import com.juul.kable.Peripheral
import com.juul.kable.PlatformAdvertisement
import com.juul.kable.logs.Logging.Level.Data
import com.juul.sensortag.bluetooth.requirements.BluetoothRequirements
import com.juul.sensortag.bluetooth.requirements.Deficiency.BluetoothOff
import com.juul.sensortag.bluetooth.requirements.Deficiency.LocationServicesDisabled
import com.juul.sensortag.features.scan.DeviceLocator.State.NotYetScanned
import com.juul.sensortag.features.scan.DeviceLocator.State.Scanning
import com.juul.sensortag.features.sensor.SensorScreen
import com.juul.sensortag.peripheral
import com.juul.sensortag.permissions.Permission
import com.juul.sensortag.permissions.PermissionState
import com.juul.sensortag.permissions.PermissionsController
import com.juul.sensortag.requestPermission
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScanScreenModel(
    val permissionsController: PermissionsController,
    bluetoothRequirements: BluetoothRequirements,
) : ScreenModel {

    private val _navigation = MutableSharedFlow<Screen>()
    val navigation = _navigation.asSharedFlow()

    private val _showConnectPermissionAlertDialog = MutableStateFlow(false)
    val showConnectPermissionAlertDialog = _showConnectPermissionAlertDialog.asStateFlow()

    private val _snackbarText = MutableStateFlow<String?>(null)
    val snackbarText = _snackbarText.asStateFlow()

    private val isBluetoothSupported = MutableStateFlow<Boolean?>(null)
    private val permissionState = MutableStateFlow(PermissionState.NotDetermined)
    private val isPermissionGranted = permissionState.map { it == PermissionState.Granted }

    /** `null` until scan permission has been requested. */
    private val isRequestingScanPermission = MutableStateFlow<Boolean?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val requirements = isPermissionGranted.flatMapLatest { isGranted ->
        bluetoothRequirements.deficiencies.takeIf { isGranted } ?: flowOf(null)
    }

    private val deviceLocator = DeviceLocator(
        onRequestDeviceSuccess = { _navigation.emit(SensorScreen()) }, // JavaScript-only
        onStatus = _snackbarText::emit,
    )

    val viewState: StateFlow<ViewState> = combine(
        isBluetoothSupported,
        permissionState,
        requirements,
        deviceLocator.state,
        deviceLocator.advertisements,
    ) { isSupported, permissionState, requirements, scanState, advertisements ->
        when (isSupported) {
            false -> ViewState.Unsupported
            true -> when (permissionState) {
                PermissionState.NotDetermined, PermissionState.NotGranted, PermissionState.Denied -> ViewState.Scan
                PermissionState.Granted -> when {
                    requirements == null -> null
                    scanState == NotYetScanned -> ViewState.Scan
                    BluetoothOff in requirements -> ViewState.BluetoothOff
                    LocationServicesDisabled in requirements -> ViewState.LocationServicesDisabled
                    else -> ViewState.Devices(scanState, advertisements)
                }
                PermissionState.DeniedAlways -> ViewState.PermissionDenied
                else -> error("Unhandled permission state: $permissionState")
            }
            null -> ViewState.Scan
        }
    }.filterNotNull()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(), ViewState.Scan)

    init {
        screenModelScope.launch {
            @OptIn(ExperimentalApi::class)
            isBluetoothSupported.value = Bluetooth.isSupported()
        }
    }

    /**
     * Re-check permissions when screen resumes (can occur after coming back from app settings
     * screen where user may have granted needed permissions).
     */
    fun onResumed() {
        screenModelScope.launch {
            when (isRequestingScanPermission.value) {
                // After requesting permission, onResume is triggered, so we reset the
                // "is requesting" state here.
                true -> isRequestingScanPermission.value = false

                false -> requestAndUpdateScanPermission()

                // On Apple (until authorized) even checking the permission state will show a
                // permission dialog. We guard against showing the dialog prior to actually wanting
                // to request the permission by only checking permission after we've explicitly
                // requested permission.
                null -> {} // No-op
            }
        }
    }

    fun scan() {
        if (deviceLocator.state.value == Scanning) return // Scan already in progress.

        if (permissionState.value == PermissionState.Granted) {
            deviceLocator.run()
        } else {
            screenModelScope.launch {
                requestAndUpdateScanPermission()
                if (permissionState.value == PermissionState.Granted) {
                    deviceLocator.run()
                }
            }
        }
    }

    fun onAdvertisementClicked(advertisement: PlatformAdvertisement) {
        screenModelScope.launch {
            when (requestConnectPermission()) {
                PermissionState.Granted -> navigateToSensorScreen(advertisement)
                PermissionState.DeniedAlways -> _showConnectPermissionAlertDialog.value = true
                else -> {} // No-op
            }
        }
    }

    fun clear() {
        screenModelScope.launch {
            _snackbarText.value = null
            deviceLocator.clear()
        }
    }

    fun openAppSettings() {
        permissionsController.openAppSettings()
    }

    fun dismissAlert() {
        _showConnectPermissionAlertDialog.value = false
    }

    private suspend fun navigateToSensorScreen(advertisement: PlatformAdvertisement) {
        deviceLocator.cancelAndJoin()
        peripheral = Peripheral(advertisement) {
            logging { level = Data }
        }
        _navigation.emit(SensorScreen())
    }

    private suspend fun requestAndUpdateScanPermission() {
        // Once we've been granted permission we no longer need to request permission. Apple and
        // Android will kill the app if permissions are revoked.
        if (permissionState.value == PermissionState.Granted) return

        isRequestingScanPermission.value = true
        permissionsController.requestPermission(Permission.BLUETOOTH_SCAN)?.let {
            permissionState.value = it
        }
    }

    private suspend fun requestConnectPermission() =
        permissionsController.requestPermission(Permission.BLUETOOTH_CONNECT)
}
