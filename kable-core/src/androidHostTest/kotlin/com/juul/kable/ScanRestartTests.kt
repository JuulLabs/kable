package com.juul.kable

import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.os.Build
import com.juul.kable.bluetooth.checkBluetoothIsOn
import com.juul.kable.logs.Logging
import com.juul.kable.scan.requirements.checkLocationServicesEnabled
import com.juul.kable.scan.requirements.checkScanPermissions
import com.juul.kable.scan.requirements.requireBluetoothLeScanner
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class ScanRestartTests {

    private val startedFilters = mutableListOf<List<ScanFilter>>()
    private val startedSettings = mutableListOf<ScanSettings>()
    private val startedCallbacks = mutableListOf<ScanCallback>()
    private val scanner = mockk<BluetoothLeScanner> {
        every { startScan(capture(startedFilters), capture(startedSettings), capture(startedCallbacks)) } just Runs
        every { stopScan(any<ScanCallback>()) } just Runs
    }

    @BeforeTest
    fun setUp() {
        mockkStatic(
            ::requireBluetoothLeScanner,
            ::checkScanPermissions,
            ::checkLocationServicesEnabled,
            ::checkBluetoothIsOn,
        )
        every { requireBluetoothLeScanner() } returns scanner
        every { checkScanPermissions() } just Runs
        every { checkLocationServicesEnabled() } just Runs
        every { checkBluetoothIsOn() } just Runs
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun filteredLowLatencyScan_restartsWithSameArguments_untilCancelled() = runTest {
        val job = launch { androidScanner(ScanSettings.SCAN_MODE_LOW_LATENCY, serviceFilter).advertisements.collect {} }
        runCurrent()
        assertStarts(1)

        advanceTimeBy(SCAN_RESTART_INTERVAL)
        runCurrent()
        assertStarts(2)
        verify(exactly = 1) { scanner.stopScan(startedCallbacks[0]) }
        assertEquals(startedFilters[0], startedFilters[1])
        assertSame(startedSettings[0], startedSettings[1])
        assertSame(startedCallbacks[0], startedCallbacks[1])

        job.cancel()
        runCurrent()
        advanceTimeBy(SCAN_RESTART_INTERVAL * 3)
        runCurrent()
        assertStarts(2)
        verify(exactly = 2) { scanner.stopScan(startedCallbacks[0]) }
    }

    @Test
    fun unfilteredLowPowerScan_restarts() = runTest {
        collectFor(ScanSettings.SCAN_MODE_LOW_POWER, filters = emptyList())
        assertStarts(2)
    }

    @Test
    fun filteredLowPowerScan_isNotRestarted() = runTest {
        collectFor(ScanSettings.SCAN_MODE_LOW_POWER, serviceFilter)
        assertStarts(1)
    }

    @Test
    fun opportunisticScan_isNotRestarted() = runTest {
        collectFor(ScanSettings.SCAN_MODE_OPPORTUNISTIC, filters = emptyList())
        assertStarts(1)
    }

    private fun androidScanner(scanMode: Int, filters: List<FilterPredicate>) = BluetoothLeScannerAndroidScanner(
        filters = filters,
        scanSettings = ScanSettings.Builder().setScanMode(scanMode).build(),
        bufferCapacity = UNLIMITED,
        logging = Logging(),
    )

    private fun TestScope.collectFor(scanMode: Int, filters: List<FilterPredicate>) {
        val job = launch { androidScanner(scanMode, filters).advertisements.collect {} }
        advanceTimeBy(SCAN_RESTART_INTERVAL)
        runCurrent()
        job.cancel()
    }

    private fun assertStarts(count: Int) {
        verify(exactly = count) {
            scanner.startScan(any<List<ScanFilter>>(), any<ScanSettings>(), any<ScanCallback>())
        }
    }

    private val serviceFilter = listOf(FilterPredicate(listOf(Filter.Service(Bluetooth.BaseUuid + 1))))
}
