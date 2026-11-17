package com.juul.kable

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCallback
import android.content.Context
import android.os.Build
import android.os.Handler
import com.juul.kable.logs.Logging
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class BluetoothDeviceTests {

    @Test
    fun connect_securityException_wrapsInIllegalStateExceptionAndReleasesThreading() {
        val securityException = SecurityException("Missing BLUETOOTH_CONNECT permission")
        val device = mockk<BluetoothDevice> {
            every { address } returns "00:11:22:AA:BB:CC"
            every {
                connectGatt(
                    any<Context>(),
                    false,
                    any<BluetoothGattCallback>(),
                    any<Int>(),
                    any<Int>(),
                    any<Handler>(),
                )
            } throws securityException
        }
        val threading = mockk<Threading.Handler> {
            every { handler } returns mockk()
        }
        val threadingStrategy = mockk<ThreadingStrategy> {
            every { acquire() } returns threading
            every { release(threading) } just Runs
        }
        every { threading.strategy } returns threadingStrategy

        val exception = assertFailsWith<IllegalStateException> {
            device.connect(
                coroutineContext = EmptyCoroutineContext,
                context = RuntimeEnvironment.getApplication(),
                autoConnect = false,
                transport = Transport.Auto,
                phy = Phy.Le1M,
                state = MutableStateFlow(State.Disconnected()),
                services = MutableStateFlow<List<PlatformDiscoveredService>?>(null),
                mtu = MutableStateFlow<Int?>(null),
                onCharacteristicChanged = MutableSharedFlow<ObservationEvent<ByteArray>>(),
                logging = Logging(),
                threadingStrategy = threadingStrategy,
                disconnectTimeout = 1.seconds,
            )
        }

        assertEquals(securityException.message, exception.message)
        assertSame(securityException, exception.cause)
        verify(exactly = 1) { threadingStrategy.release(threading) }
    }
}
