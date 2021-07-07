package com.juul.sensortag.features.scan

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.juul.kable.Advertisement
import com.juul.sensortag.R
import com.juul.sensortag.databinding.ScanBinding
import com.juul.sensortag.features.scan.ScanStatus.Failed
import com.juul.sensortag.features.scan.ScanStatus.Started
import com.juul.sensortag.features.scan.ScanStatus.Stopped
import com.juul.sensortag.features.sensor.SensorActivityIntent
import com.juul.sensortag.observe
import com.juul.tuulbox.logging.Log

class ScanActivity : AppCompatActivity() {

    private val viewModel by viewModels<ScanViewModel>()
    private lateinit var adapter: ScanAdapter
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val listener = { advertisement: Advertisement ->
            viewModel.stopScan()
            val intent = SensorActivityIntent(
                context = this@ScanActivity,
                macAddress = advertisement.address
            )
            startActivity(intent)
        }
        adapter = ScanAdapter(listener).apply {
            setHasStableIds(true)
        }

        observe(viewModel.advertisements.asLiveData()) {
            adapter.update(it)
        }

        observe(viewModel.scanStatus.asLiveData()) { status ->
            Log.debug { "Scan status: $status" }

            when (status) {
                Started -> showSnackbar("Scanning")
                Stopped -> dismissSnackbar()
                is Failed -> {
                    dismissSnackbar()
                    showAlert("Scan failed!\n${status.message}")
                }
            }
        }

        ScanBinding.inflate(layoutInflater).apply {
            scanList.layoutManager = LinearLayoutManager(this@ScanActivity)
            scanList.adapter = adapter
            setContentView(root)
        }
    }

    override fun onCreateOptionsMenu(
        menu: Menu?,
    ): Boolean {
        menuInflater.inflate(R.menu.scan, menu)
        return true
    }

    override fun onOptionsItemSelected(
        item: MenuItem,
    ): Boolean {
        when (item.itemId) {
            R.id.refresh -> scan()
            R.id.clear -> {
                viewModel.stopScan()
                adapter.update(emptyList())
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun scan() {
        when {
            !isBluetoothEnabled -> enableBluetooth()
            !hasLocationPermission -> requestLocationPermission()
            else -> viewModel.startScan()
        }
    }

    private data class SnackbarAction(
        val text: CharSequence,
        val action: View.OnClickListener,
    )

    private fun showSnackbar(
        text: CharSequence,
        action: SnackbarAction? = null,
    ) {
        snackbar = Snackbar
            .make(findViewById(R.id.scan_list), text, Snackbar.LENGTH_INDEFINITE)
            .apply {
                if (action != null) setAction(action.text, action.action)
                show()
            }
    }

    private fun dismissSnackbar() {
        snackbar?.dismiss()
        snackbar = null
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopScan()
    }
}

private fun Context.showAlert(message: CharSequence) {
    AlertDialog.Builder(this)
        .setMessage(message)
        .show()
}

private val isBluetoothEnabled: Boolean
    get() = BluetoothAdapter.getDefaultAdapter().isEnabled

private fun Activity.enableBluetooth() {
    val intent = Intent(ACTION_REQUEST_ENABLE)
    startActivityForResult(intent, RequestCode.EnableBluetooth)
}

private object RequestCode {
    const val EnableBluetooth = 55001
    const val LocationPermission = 55002
}

private val Context.hasLocationPermission: Boolean
    get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
        hasPermission(ACCESS_COARSE_LOCATION) ||
        hasPermission(ACCESS_FINE_LOCATION)

private fun Context.hasPermission(
    permission: String,
): Boolean = ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED

/**
 * Shows the native Android permission request dialog.
 *
 * The result of the dialog will come back via [Activity.onRequestPermissionsResult] method.
 */
private fun Activity.requestLocationPermission() {
    /*   .-----------------------------.
     *   |   _                         |
     *   |  /o\  Allow App to access   |
     *   |  \ /  access this device's  |
     *   |   v   location?             |
     *   |                             |
     *   |  [ ] Don't ask again        |
     *   |                             |
     *   |               DENY   ALLOW  |
     *   '-----------------------------'
     *
     * "Don't ask again" checkbox is not shown on the first request, but on all subsequent requests (after a DENY).
     */
    val permissions = arrayOf(ACCESS_FINE_LOCATION)
    ActivityCompat.requestPermissions(this, permissions, RequestCode.LocationPermission)
}
