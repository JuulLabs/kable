package com.kable.androidsample

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.Advertisement
import com.juul.kable.Characteristic
import com.juul.kable.DiscoveredService
import com.juul.kable.Filter
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.logs.Logging
import com.juul.kable.logs.SystemLogEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val _advertisement: MutableStateFlow<List<Advertisement>> = MutableStateFlow(emptyList())
    val advertisement: StateFlow<List<Advertisement>> = _advertisement
    val _permissions: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val permissions: StateFlow<Boolean> = _permissions

    val _peripheral: MutableStateFlow<Peripheral?> = MutableStateFlow(null)
    val peripheral: StateFlow<Peripheral?> = _peripheral

    val _services: MutableStateFlow<List<DiscoveredService>> = MutableStateFlow(emptyList())
    val services: StateFlow<List<DiscoveredService>> = _services


    val _serviceSelected: MutableStateFlow<DiscoveredService?> = MutableStateFlow(null)
    val serviceSelected: StateFlow<DiscoveredService?> = _serviceSelected

    val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading


    val scanner = Scanner {
  /*      filters {
            match {
                name = Filter.Name.Prefix("The")
            }
        }*/
        logging {
            engine = SystemLogEngine
            level = Logging.Level.Warnings
            format = Logging.Format.Multiline
        }
    }

    fun selectService(serviceDiscovered: DiscoveredService) {
        _serviceSelected.value = serviceDiscovered
    }

    fun startScan() {
        viewModelScope.launch {
            scanner.advertisements
                .filter { _peripheral.value == null }
                .onEach {
                    //replace or insert and log
                    Log.d("MainViewModel", "Advertisement: ${it.name}")
                    _advertisement.value = _advertisement.value
                        .associateBy { it.name }
                        .toMutableMap()
                        .apply { put(it.name, it) }
                        .values
                        .toList()
                }
                .launchIn(viewModelScope)
        }
    }


    fun connect(adv: Advertisement) {
        _loading.value = true
        viewModelScope.launch {
            try {
                _peripheral.value = Peripheral(adv) {
                    logging {
                        engine = SystemLogEngine
                        level = Logging.Level.Warnings
                        format = Logging.Format.Multiline
                    }
                }.apply {
                    connect(false)
                    services
                        .onEach {
                            _services.value = it?.toList() ?: emptyList()
                        }
                        .launchIn(
                            viewModelScope,
                        )
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }
            _loading.value = false
        }
    }


    fun permissionGranted() {
        _permissions.value = true
    }

    fun readCharacteristic(characteristic: Characteristic) {
        viewModelScope.launch {
            val readed = _peripheral.value?.read(characteristic)
            Log.d("MainViewModel", "Read characteristic: ${readed?.toString()}")
        }
    }


}