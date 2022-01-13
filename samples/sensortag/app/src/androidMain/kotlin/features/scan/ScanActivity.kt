@file:OptIn(ExperimentalPermissionsApi::class)

package com.juul.sensortag.features.scan

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED
import android.bluetooth.BluetoothAdapter.ERROR
import android.bluetooth.BluetoothAdapter.EXTRA_STATE
import android.bluetooth.BluetoothAdapter.STATE_CONNECTED
import android.bluetooth.BluetoothAdapter.STATE_CONNECTING
import android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED
import android.bluetooth.BluetoothAdapter.STATE_DISCONNECTING
import android.bluetooth.BluetoothAdapter.STATE_ON
import android.bluetooth.BluetoothAdapter.STATE_TURNING_ON
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.juul.kable.Advertisement
import com.juul.sensortag.AppTheme
import com.juul.sensortag.enableBluetooth
import com.juul.sensortag.features.scan.ScanStatus.Failed
import com.juul.sensortag.features.scan.ScanStatus.Scanning
import com.juul.sensortag.features.scan.ScanStatus.Stopped
import com.juul.sensortag.features.sensor.SensorActivityIntent
import com.juul.sensortag.icons.BluetoothDisabled
import com.juul.sensortag.icons.LocationDisabled
import com.juul.sensortag.openAppDetails
import com.juul.tuulbox.coroutines.flow.broadcastReceiverFlow
import kotlinx.coroutines.flow.map

class ScanActivity : ComponentActivity() {

    private val isBluetoothEnabled = broadcastReceiverFlow(IntentFilter(ACTION_STATE_CHANGED))
        .map { intent -> intent.getIsBluetoothEnabled() }

    private val viewModel by viewModels<ScanViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val isBluetoothEnabled = isBluetoothEnabled
                    .collectAsState(initial = BluetoothAdapter.getDefaultAdapter().isEnabled)
                    .value

                Column(Modifier.background(color = MaterialTheme.colors.background)) {
                    AppBar(viewModel, isBluetoothEnabled)

                    Box(Modifier.weight(1f)) {
                        ProvideTextStyle(
                            TextStyle(color = contentColorFor(backgroundColor = MaterialTheme.colors.background))
                        ) {
                            val permissions = listOf(ACCESS_FINE_LOCATION, BLUETOOTH_SCAN, BLUETOOTH_CONNECT)
                            val permissionsState = rememberMultiplePermissionsState(permissions)
                            PermissionsRequired(
                                multiplePermissionsState = permissionsState,
                                permissionsNotGrantedContent = { BluetoothPermissionsNotGranted(permissionsState) },
                                permissionsNotAvailableContent = { BluetoothPermissionsNotAvailable(::openAppDetails) }
                            ) {
                                if (isBluetoothEnabled) {
                                    val advertisements = viewModel.advertisements.collectAsState().value
                                    AdvertisementsList(advertisements, ::onAdvertisementClicked)
                                } else {
                                    BluetoothDisabled(::enableBluetooth)
                                }
                            }
                        }

                        StatusSnackbar(viewModel)
                    }
                }
            }
        }
    }

    private fun onAdvertisementClicked(advertisement: Advertisement) {
        viewModel.stop()
        val intent = SensorActivityIntent(
            context = this@ScanActivity,
            macAddress = advertisement.address
        )
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }
}

@Composable
private fun AppBar(viewModel: ScanViewModel, isBluetoothEnabled: Boolean) {
    val status = viewModel.status.collectAsState().value

    TopAppBar(
        title = {
            Text("SensorTag Example")
        },
        actions = {
            if (isBluetoothEnabled) {
                if (status !is Scanning) {
                    IconButton(onClick = viewModel::start) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
                IconButton(onClick = viewModel::clear) {
                    Icon(Icons.Filled.Delete, contentDescription = "Clear")
                }
            }
        }
    )
}

@Composable
private fun BoxScope.StatusSnackbar(viewModel: ScanViewModel) {
    val status = viewModel.status.collectAsState().value

    if (status !is Stopped) {
        val text = when (status) {
            is Scanning -> "Scanning"
            is Stopped -> "Idle"
            is Failed -> "Error: ${status.message}"
        }
        Snackbar(
            Modifier
                .align(BottomCenter)
                .padding(10.dp)
        ) {
            Text(text, style = MaterialTheme.typography.body1)
        }
    }
}

@Composable
private fun ActionRequired(
    icon: ImageVector,
    contentDescription: String?,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Center,
    ) {
        Icon(
            modifier = Modifier.size(150.dp),
            tint = contentColorFor(backgroundColor = MaterialTheme.colors.background),
            imageVector = icon,
            contentDescription = contentDescription,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            modifier = Modifier.fillMaxWidth().align(CenterHorizontally),
            textAlign = TextAlign.Center,
            text = description,
        )
        Spacer(Modifier.size(15.dp))
        Button(onClick) {
            Text(buttonText)
        }
    }
}

@Composable
private fun BluetoothDisabled(enableAction: () -> Unit) {
    ActionRequired(
        icon = Icons.Filled.BluetoothDisabled,
        contentDescription = "Bluetooth disabled",
        description = "Bluetooth is disabled.",
        buttonText = "Enable",
        onClick = enableAction,
    )
}

@Composable
private fun BluetoothPermissionsNotGranted(permissions: MultiplePermissionsState) {
    ActionRequired(
        icon = Icons.Filled.LocationDisabled,
        contentDescription = "Bluetooth permissions required",
        description = "Bluetooth permissions are required for scanning. Please grant the permission.",
        buttonText = "Continue",
        onClick = permissions::launchMultiplePermissionRequest,
    )
}

@Composable
private fun BluetoothPermissionsNotAvailable(openSettingsAction: () -> Unit) {
    ActionRequired(
        icon = Icons.Filled.Warning,
        contentDescription = "Bluetooth permissions required",
        description = "Bluetooth permission denied. Please, grant access on the Settings screen.",
        buttonText = "Open Settings",
        onClick = openSettingsAction,
    )
}

@Composable
private fun AdvertisementsList(
    advertisements: List<Advertisement>,
    onRowClick: (Advertisement) -> Unit
) {
    LazyColumn {
        items(advertisements.size) { index ->
            val advertisement = advertisements[index]
            AdvertisementRow(advertisement) { onRowClick(advertisement) }
        }
    }
}

@Composable
private fun AdvertisementRow(advertisement: Advertisement, onClick: () -> Unit) {
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
            Text(advertisement.address)
        }

        Text(
            modifier = Modifier.align(CenterVertically),
            text = "${advertisement.rssi} dBm",
        )
    }
}

private fun Intent.getIsBluetoothEnabled(): Boolean = when (getIntExtra(EXTRA_STATE, ERROR)) {
    STATE_TURNING_ON, STATE_ON, STATE_CONNECTING, STATE_CONNECTED, STATE_DISCONNECTING, STATE_DISCONNECTED -> true
    else -> false // STATE_TURNING_OFF, STATE_OFF
}
