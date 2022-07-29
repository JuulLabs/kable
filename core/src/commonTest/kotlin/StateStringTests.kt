package com.juul.kable

import com.juul.kable.State.Connected
import com.juul.kable.State.Connecting.Bluetooth
import com.juul.kable.State.Connecting.Observes
import com.juul.kable.State.Connecting.Services
import com.juul.kable.State.Disconnected
import com.juul.kable.State.Disconnected.Status.EncryptionTimedOut
import com.juul.kable.State.Disconnected.Status.PeripheralDisconnected
import com.juul.kable.State.Disconnected.Status.CentralDisconnected
import com.juul.kable.State.Disconnected.Status.Failed
import com.juul.kable.State.Disconnected.Status.L2CapFailure
import com.juul.kable.State.Disconnected.Status.Timeout
import com.juul.kable.State.Disconnected.Status.LinkManagerProtocolTimeout
import com.juul.kable.State.Disconnected.Status.UnknownDevice
import com.juul.kable.State.Disconnected.Status.Cancelled
import com.juul.kable.State.Disconnected.Status.ConnectionLimitReached
import com.juul.kable.State.Disconnected.Status.Unknown
import com.juul.kable.State.Disconnecting
import kotlin.test.Test
import kotlin.test.assertEquals

public class StateStringTests {

    private fun test(expected: String, actual: Any) {
        assertEquals(expected, actual.toString())
    }

    @Test
    fun testToString() {
        test("Connected", Connected)
        test("Connecting.Bluetooth", Bluetooth)
        test("Connecting.Observes", Observes)
        test("Connecting.Services", Services)
        test("Disconnecting", Disconnecting)
        test("Disconnected(Peripheral Disconnected)", Disconnected(PeripheralDisconnected))
        test("Disconnected(Central Disconnected)", Disconnected(CentralDisconnected))
        test("Disconnected(Failed)", Disconnected(Failed))
        test("Disconnected(L2Cap Failure)", Disconnected(L2CapFailure))
        test("Disconnected(Timeout)", Disconnected(Timeout))
        test("Disconnected(LinkManager Protocol Timeout)", Disconnected(LinkManagerProtocolTimeout))
        test("Disconnected(Unknown Device)", Disconnected(UnknownDevice))
        test("Disconnected(Cancelled)", Disconnected(Cancelled))
        test("Disconnected(Connection Limit Reached)", Disconnected(ConnectionLimitReached))
        test("Disconnected(Encryption Timed Out)", Disconnected(EncryptionTimedOut))
        test("Disconnected(133)", Disconnected(Unknown(133)))
        test("Unknown(status=133)", Unknown(133))
    }

}

private infix fun State.assertToStringEquals(expected: String) {
    assertEquals(
        expected = expected,
        actual = toString(),
    )
}
