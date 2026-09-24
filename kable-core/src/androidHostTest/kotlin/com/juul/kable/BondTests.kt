package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.bluetooth.BluetoothDevice.BOND_BONDING
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.bluetooth.BluetoothDevice.ERROR
import android.bluetooth.BluetoothDevice.EXTRA_BOND_STATE
import android.bluetooth.BluetoothDevice.EXTRA_DEVICE
import android.content.Intent
import android.os.Build
import android.os.Looper
import com.juul.kable.AndroidPeripheral.Bond
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class BondTests {

    private val app = RuntimeEnvironment.getApplication()

    @BeforeTest
    fun setUp() {
        KableInitializer().create(app)
    }

    @Test
    fun bond_alreadyBonded_returnsBondedWithoutCreatingBond() = runTest {
        val device = device(initial = BOND_BONDED)

        assertEquals(Bond.Bonded, device.createBondAndAwait())
        verify(exactly = 0) { device.createBond() }
    }

    @Test
    fun bond_createBondRefused_returnsNone() = runTest {
        val device = device(initial = BOND_NONE, createBond = false)

        assertEquals(Bond.None, device.createBondAndAwait())
    }

    @Test
    fun bond_bondingThenBonded_returnsBonded() = runTest {
        val device = device(initial = BOND_NONE)

        val result = bondWithBroadcasts(device, device to BOND_BONDING, device to BOND_BONDED)

        assertEquals(Bond.Bonded, result)
    }

    @Test
    fun bond_bondingThenNone_returnsNone() = runTest {
        val device = device(initial = BOND_NONE)

        val result = bondWithBroadcasts(device, device to BOND_BONDING, device to BOND_NONE)

        assertEquals(Bond.None, result)
    }

    @Test
    fun bond_ignoresBroadcastsForOtherDevices() = runTest {
        val device = device(initial = BOND_NONE)
        val other = device(initial = BOND_NONE)

        val result = bondWithBroadcasts(device, other to BOND_BONDED, device to BOND_NONE)

        assertEquals(Bond.None, result)
    }

    @Test
    fun bondFromState_unknownState_isNull() {
        assertNull(Bond(ERROR))
    }

    private suspend fun TestScope.bondWithBroadcasts(
        device: BluetoothDevice,
        vararg broadcasts: Pair<BluetoothDevice, Int>,
    ): Bond {
        val result = async { device.createBondAndAwait() }
        runCurrent() // Registers the receiver and calls `createBond()`.
        broadcasts.forEach { (target, state) -> app.sendBroadcast(bondStateChanged(target, state)) }
        shadowOf(Looper.getMainLooper()).idle()
        return result.await()
    }

    private fun device(initial: Int, createBond: Boolean = true) = mockk<BluetoothDevice> {
        every { bondState } returns initial
        every { createBond() } returns createBond
    }

    private fun bondStateChanged(device: BluetoothDevice, state: Int) =
        Intent(ACTION_BOND_STATE_CHANGED)
            .putExtra(EXTRA_DEVICE, device)
            .putExtra(EXTRA_BOND_STATE, state)
}
