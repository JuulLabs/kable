package com.juul.kable.gatt

import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.os.Build
import com.juul.kable.NotConnectedException
import com.juul.kable.ObservationEvent
import com.juul.kable.State
import com.juul.kable.external.GATT_CONN_TIMEOUT
import com.juul.kable.logs.Logging
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class CallbackTests {

    @Test
    fun onConnectionStateChange_disconnectWithTimeoutStatus_closesResponsesWithStatusInNotConnectedException() = runTest {
        val callback = callback()

        callback.onConnectionStateChange(mockk(), GATT_CONN_TIMEOUT, STATE_DISCONNECTED)

        val exception = assertFailsWith<NotConnectedException> {
            callback.onResponse.receive()
        }
        assertEquals(State.Disconnected.Status.Timeout, exception.status)
    }

    @Test
    fun onConnectionStateChange_disconnectWithUnknownStatus_closesResponsesWithStatusInNotConnectedException() = runTest {
        val callback = callback()
        val gattInsufficientAuthentication = 5

        callback.onConnectionStateChange(mockk(), gattInsufficientAuthentication, STATE_DISCONNECTED)

        val exception = assertFailsWith<NotConnectedException> {
            callback.onResponse.receive()
        }
        assertEquals(
            State.Disconnected.Status.Unknown(gattInsufficientAuthentication),
            exception.status,
        )
    }

    @Test
    fun onConnectionStateChange_disconnectWithSuccessStatus_closesResponsesWithoutStatusInNotConnectedException() = runTest {
        val callback = callback()

        callback.onConnectionStateChange(mockk(), GATT_SUCCESS, STATE_DISCONNECTED)

        val exception = assertFailsWith<NotConnectedException> {
            callback.onResponse.receive()
        }
        assertNull(exception.status)
    }

    private fun callback() = Callback(
        state = MutableStateFlow(State.Connecting.Services),
        mtu = MutableStateFlow(null),
        onCharacteristicChanged = MutableSharedFlow<ObservationEvent<ByteArray>>(),
        logging = Logging(),
        macAddress = "00:11:22:AA:BB:CC",
    )
}
