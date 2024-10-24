package com.juul.sensortag.features.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.juul.kable.PlatformAdvertisement
import com.juul.sensortag.AppTheme
import com.juul.sensortag.bluetooth.rememberSystemControl
import com.juul.sensortag.bluetooth.requirements.rememberBluetoothRequirementsFactory
import com.juul.sensortag.features.components.ActionRequired
import com.juul.sensortag.features.components.BluetoothDisabled
import com.juul.sensortag.features.scan.DeviceLocator.State.NotYetScanned
import com.juul.sensortag.features.scan.DeviceLocator.State.Scanning
import com.juul.sensortag.icons.LocationDisabled
import com.juul.sensortag.permissions.BindEffect
import com.juul.sensortag.permissions.rememberPermissionsControllerFactory

class ScanScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel()
        onLifecycleResumed(screenModel::onResumed)
        handleNavigation(screenModel)

        val viewState by screenModel.viewState.collectAsState()
        val showConnectPermissionAlertDialog by screenModel.showConnectPermissionAlertDialog.collectAsState()
        val snackbarText by screenModel.snackbarText.collectAsState()

        AppTheme {
            Column(
                Modifier
                    .background(color = MaterialTheme.colors.background)
                    .fillMaxSize()
            ) {
                AppBar(
                    viewState,
                    onRefreshClick = screenModel::scan,
                    onClearClick = screenModel::clear,
                )
                Box(Modifier.weight(1f)) {
                    ProvideTextStyle(
                        TextStyle(color = contentColorFor(backgroundColor = MaterialTheme.colors.background))
                    ) {
                        val systemControl = rememberSystemControl()
                        ScanPane(
                            viewState,
                            onScanClick = screenModel::scan,
                            onShowAppSettingsClick = screenModel::openAppSettings,
                            onShowLocationSettingsClick = systemControl::showLocationSettings,
                            onEnableBluetoothClick = systemControl::requestToTurnBluetoothOn,
                            onAdvertisementClicked = screenModel::onAdvertisementClicked,
                        )
                    }

                    snackbarText?.let { text ->
                        Snackbar(text)
                    }

                    if (showConnectPermissionAlertDialog) {
                        ConnectPermissionsAlertDialog(
                            onOpenAppSettings = screenModel::openAppSettings,
                            onCancel = screenModel::dismissAlert,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun rememberScreenModel(): ScanScreenModel {
        val permissionsFactory = rememberPermissionsControllerFactory()
        val bluetoothRequirementsFactory = rememberBluetoothRequirementsFactory()
        val screenModel = rememberScreenModel {
            val permissionsController = permissionsFactory.createPermissionsController()
            val bluetoothRequirements = bluetoothRequirementsFactory.create()
            ScanScreenModel(permissionsController, bluetoothRequirements)
        }
        BindEffect(screenModel.permissionsController)
        return screenModel
    }
}

@Composable
private fun handleNavigation(screenModel: ScanScreenModel) {
    val navigator = LocalNavigator.currentOrThrow
    LaunchedEffect(screenModel) {
        screenModel.navigation.collect(navigator::push)
    }
}

@Composable
private fun BoxScope.Snackbar(text: String) {
    Snackbar(
        Modifier
            .align(BottomCenter)
            .padding(10.dp)
    ) {
        Text(text, style = MaterialTheme.typography.body1)
    }
}

@Composable
private fun ConnectPermissionsAlertDialog(
    onOpenAppSettings: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        title = { Text("Permission required") },
        text = { Text("Bluetooth connect permission needed to connect to device. Please grant permission via App settings.") },
        confirmButton = {
            TextButton(onClick = onOpenAppSettings) {
                Text("Open App Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        },
        onDismissRequest = onCancel,
    )
}

@Composable
private fun ScanPane(
    viewState: ViewState,
    onScanClick: () -> Unit,
    onShowAppSettingsClick: () -> Unit,
    onShowLocationSettingsClick: () -> Unit,
    onEnableBluetoothClick: () -> Unit,
    onAdvertisementClicked: (PlatformAdvertisement) -> Unit,
) {
    when (viewState) {
        is ViewState.Unsupported -> BluetoothNotSupported()
        is ViewState.Scan -> Scan(message = null, onScanClick)
        is ViewState.PermissionDenied -> BluetoothPermissionsDenied(onShowAppSettingsClick)
        is ViewState.LocationServicesDisabled -> LocationServicesDisabled(onShowLocationSettingsClick)
        is ViewState.BluetoothOff -> BluetoothDisabled(onEnableBluetoothClick)
        is ViewState.Devices -> AdvertisementsList(
            viewState.scanState,
            viewState.advertisements,
            onScanClick,
            onAdvertisementClicked
        )
    }
}

@Composable
private fun BluetoothNotSupported() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Bluetooth not supported.")
    }
}

@Composable
private fun Loading() {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun Scan(message: String?, onScanClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = CenterHorizontally,) {
            if (message != null) Text(message)
            Button(onScanClick) {
                Text("Scan")
            }
        }
    }
}

@Composable
private fun AppBar(
    viewState: ViewState,
    onRefreshClick: () -> Unit,
    onClearClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text("SensorTag Example")
        },
        actions = {
            if (viewState == ViewState.Scan || (viewState as? ViewState.Devices)?.scanState != Scanning) {
                IconButton(onClick = onRefreshClick) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
            }
            IconButton(onClick = onClearClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Clear")
            }
        }
    )
}

@Composable
private fun LocationServicesDisabled(enableAction: () -> Unit) {
    ActionRequired(
        icon = Icons.Filled.LocationDisabled,
        contentDescription = "Location services disabled",
        description = "Location services are disabled.",
        buttonText = "Enable",
        onClick = enableAction,
    )
}

@Composable
private fun BluetoothPermissionsDenied(onShowAppSettingsClick: () -> Unit) {
    ActionRequired(
        icon = Icons.Filled.Warning,
        contentDescription = "Bluetooth permissions required",
        description = "Bluetooth permissions are required for scanning. Please grant the permission.",
        buttonText = "Open Settings",
        onClick = onShowAppSettingsClick,
    )
}

@Composable
private fun AdvertisementsList(
    scanState: DeviceLocator.State,
    advertisements: List<PlatformAdvertisement>,
    onScanClick: () -> Unit,
    onAdvertisementClick: (PlatformAdvertisement) -> Unit,
) {
    when {
        scanState == NotYetScanned -> Scan(message = null, onScanClick)

        // Scanning or Scanned w/ advertisements found.
        advertisements.isNotEmpty() -> LazyColumn {
            items(advertisements.size) { index ->
                val advertisement = advertisements[index]
                AdvertisementRow(advertisement) { onAdvertisementClick(advertisement) }
            }
        }

        // Scanning w/ no advertisements yet found.
        scanState == Scanning -> Loading()

        // Scanned w/ no advertisements found.
        else -> Scan("No devices found.", onScanClick)
    }
}

@Composable
private fun AdvertisementRow(advertisement: PlatformAdvertisement, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                fontSize = 22.sp,
                text = advertisement.name ?: "Unknown",
            )
            Text(advertisement.identifier.toString())
        }

        Text(
            modifier = Modifier.align(CenterVertically),
            text = "${advertisement.rssi} dBm",
        )
    }
}

