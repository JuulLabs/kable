package com.kable.androidsample

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.kable.androidsample.ui.ConnectedView
import com.kable.androidsample.ui.ScannerView
import com.kable.androidsample.ui.theme.KableTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        permissions.entries.forEach {
            val isGranted = it.value
            if (!isGranted) {
                vm.permissionGranted()
            }
        }
    }
    private val vm: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        // Check and request permissions
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH,
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_SCAN,
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_CONNECT,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                ),
            )
        }

        setContent {
            KableTheme {
                val hasPermissions by vm.permissions.collectAsState()
                val loading by vm.loading.collectAsState()
                val peripheral by vm.peripheral.collectAsState()
                val services by vm.services.collectAsState()
                val serviceSelected by vm.serviceSelected.collectAsState()

                if (hasPermissions) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                    ) { innerPadding ->
                        if (loading) {
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("Loading")
                            }
                        } else if (peripheral != null) {
                            // When device is connected
                            ConnectedView(
                                modifier = Modifier.padding(innerPadding),
                                services = services,
                                selectedService = serviceSelected,
                                select = { vm.selectService(it) },
                                readCharacteristic = { vm.readCharacteristic(it) },
                            )
                        } else {
                            // When No device scan to find devices
                            LaunchedEffect("") {
                                vm.startScan()
                            }
                            val advertisement by vm.advertisement.collectAsState()
                            ScannerView(
                                modifier = Modifier.padding(innerPadding),
                                advertisement = advertisement,
                                connect = { vm.connect(it) },
                            )
                        }
                    }
                }
            }
        }
    }
}
