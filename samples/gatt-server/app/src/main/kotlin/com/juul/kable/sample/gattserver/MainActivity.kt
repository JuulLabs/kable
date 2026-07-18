@file:OptIn(ExperimentalKableApi::class)

package com.juul.kable.sample.gattserver

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juul.kable.ExperimentalKableApi
import com.juul.kable.server.GattServer.State.Started
import com.juul.kable.server.GattServer.State.Stopped
import kotlin.math.roundToInt

private val requiredPermissions =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        emptyArray() // Legacy (API < 31) bluetooth permissions are granted at install time.
    }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    App()
                }
            }
        }
    }
}

@Composable
private fun App() {
    val context = LocalContext.current
    var permissionsGranted by remember {
        mutableStateOf(
            requiredPermissions.all { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED
            },
        )
    }

    if (permissionsGranted) {
        ServerScreen()
    } else {
        val launcher = rememberLauncherForActivityResult(RequestMultiplePermissions()) { results ->
            permissionsGranted = results.values.all { it }
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Bluetooth permissions are needed to host and advertise the GATT server.")
            Button(onClick = { launcher.launch(requiredPermissions) }) {
                Text("Grant permissions")
            }
        }
    }
}

@Composable
private fun ServerScreen(viewModel: ServerViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val advertising by viewModel.advertising.collectAsState()
    val subscribers by viewModel.subscribers.collectAsState()
    val heartRate by viewModel.heartRate.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("GATT server: $state", style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = viewModel::start,
                enabled = state is Stopped,
            ) { Text("Start") }

            Button(
                onClick = viewModel::stop,
                enabled = state is Started,
            ) { Text("Stop") }

            Button(
                onClick = viewModel::toggleAdvertising,
                enabled = state is Started,
            ) { Text(if (advertising) "Stop advertising" else "Advertise") }
        }

        Text("Heart rate: $heartRate bpm")
        Slider(
            value = heartRate.toFloat(),
            onValueChange = { viewModel.setHeartRate(it.roundToInt()) },
            valueRange = 40f..200f,
        )

        Text("Subscribers: ${subscribers.size}")
    }
}
