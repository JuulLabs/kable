package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.bluetooth.BluetoothDevice.BOND_BONDING
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.bluetooth.BluetoothDevice.ERROR
import android.bluetooth.BluetoothDevice.EXTRA_BOND_STATE
import android.bluetooth.BluetoothDevice.EXTRA_DEVICE
import android.bluetooth.BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE
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
import kotlin.test.assertFailsWith
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
        val device = device(BOND_BONDED)

        assertEquals(Bond.Bonded, device.createBondAndAwait())
        verify(exactly = 0) { device.createBond() }
    }

    @Test
    fun bond_createBondRefused_returnsNone() = runTest {
        val device = device(BOND_NONE, createBond = false)

        assertEquals(Bond.None, device.createBondAndAwait())
    }

    @Test
    fun bond_bondingThenBonded_returnsBondedAndUnregisters() = runTest {
        val device = device(BOND_NONE)

        val result = bondWithBroadcasts(
            device,
            bondStateChanged(device, BOND_BONDING, previous = BOND_NONE),
            bondStateChanged(device, BOND_BONDED, previous = BOND_BONDING),
        )

        assertEquals(Bond.Bonded, result)
        assertEquals(0, bondReceiverCount())
    }

    @Test
    fun bond_bondingThenNone_returnsNone() = runTest {
        val device = device(BOND_NONE)

        val result = bondWithBroadcasts(
            device,
            bondStateChanged(device, BOND_BONDING, previous = BOND_NONE),
            bondStateChanged(device, BOND_NONE, previous = BOND_BONDING),
        )

        assertEquals(Bond.None, result)
    }

    @Test
    fun bond_ignoresBroadcastsForOtherDevices() = runTest {
        val device = device(BOND_NONE)
        val other = device(BOND_NONE)

        val result = bondWithBroadcasts(
            device,
            bondStateChanged(other, BOND_BONDED, previous = BOND_BONDING),
            bondStateChanged(device, BOND_NONE, previous = BOND_BONDING),
        )

        assertEquals(Bond.None, result)
    }

    @Test
    fun bond_noneFromEarlierRemoval_isNotTheOutcome() = runTest {
        val device = device(BOND_NONE)

        val result = bondWithBroadcasts(
            device,
            bondStateChanged(device, BOND_NONE, previous = BOND_BONDED),
            bondStateChanged(device, BOND_BONDING, previous = BOND_NONE),
            bondStateChanged(device, BOND_BONDED, previous = BOND_BONDING),
        )

        assertEquals(Bond.Bonded, result)
    }

    @Test
    fun bond_requestDroppedWithoutBroadcast_returnsNone() = runTest {
        val device = device(BOND_NONE)

        assertEquals(Bond.None, device.createBondAndAwait())
        assertEquals(0, bondReceiverCount())
    }

    @Test
    fun bond_bondedWithoutBroadcast_returnsBondedFromPoll() = runTest {
        val device = device(BOND_NONE, BOND_BONDING, BOND_BONDED)

        assertEquals(Bond.Bonded, device.createBondAndAwait())
    }

    @Test
    fun bond_bondingThenNoneWithoutBroadcast_returnsNoneFromPoll() = runTest {
        val device = device(BOND_BONDING, BOND_BONDING, BOND_NONE)

        assertEquals(Bond.None, device.createBondAndAwait())
        verify(exactly = 0) { device.createBond() }
    }

    @Test
    fun bond_bondStateThrows_rethrowsAndUnregisters() = runTest {
        val device = mockk<BluetoothDevice> {
            every { bondState } throws SecurityException("BLUETOOTH_CONNECT")
        }

        assertFailsWith<SecurityException> { device.createBondAndAwait() }
        assertEquals(0, bondReceiverCount())
    }

    @Test
    fun bond_createBondThrows_rethrowsAndUnregisters() = runTest {
        val device = mockk<BluetoothDevice> {
            every { bondState } returns BOND_NONE
            every { createBond() } throws SecurityException("BLUETOOTH_CONNECT")
        }

        assertFailsWith<SecurityException> { device.createBondAndAwait() }
        assertEquals(0, bondReceiverCount())
    }

    @Test
    fun bondFromState_unknownState_isNull() {
        assertNull(Bond(ERROR))
    }

    private suspend fun TestScope.bondWithBroadcasts(device: BluetoothDevice, vararg broadcasts: Intent): Bond {
        val result = async { device.createBondAndAwait() }
        runCurrent() // Registers the receiver and calls `createBond()`.
        broadcasts.forEach(app::sendBroadcast)
        shadowOf(Looper.getMainLooper()).idle()
        return result.await()
    }

    private fun bondReceiverCount() =
        shadowOf(app).registeredReceivers.count { it.intentFilter.hasAction(ACTION_BOND_STATE_CHANGED) }

    /** A device whose `bondState` reads [states] in turn, repeating the last. */
    private fun device(vararg states: Int, createBond: Boolean = true) = mockk<BluetoothDevice> {
        every { bondState } returnsMany states.toList()
        every { createBond() } returns createBond
    }

    private fun bondStateChanged(device: BluetoothDevice, state: Int, previous: Int) =
        Intent(ACTION_BOND_STATE_CHANGED)
            .putExtra(EXTRA_DEVICE, device)
            .putExtra(EXTRA_BOND_STATE, state)
            .putExtra(EXTRA_PREVIOUS_BOND_STATE, previous)
}
