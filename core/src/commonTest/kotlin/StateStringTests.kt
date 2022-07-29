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

    @Test
    fun testToString() {
        Connected.assertEquals("Connected")
        Bluetooth.assertEquals("Connecting.Bluetooth")
        Observes.assertEquals("Connecting.Observes")
        Services.assertEquals("Connecting.Services")
        Disconnecting.assertEquals("Disconnecting")
        Disconnected(PeripheralDisconnected).assertEquals("Disconnected(Peripheral Disconnected)")
        Disconnected(CentralDisconnected).assertEquals("Disconnected(Central Disconnected)")
        Disconnected(Failed).assertEquals("Disconnected(Failed)")
        Disconnected(L2CapFailure).assertEquals("Disconnected(L2Cap Failure)")
        Disconnected(Timeout).assertEquals("Disconnected(Timeout)")
        Disconnected(LinkManagerProtocolTimeout).assertEquals("Disconnected(LinkManager Protocol Timeout)")
        Disconnected(UnknownDevice).assertEquals("Disconnected(Unknown Device)")
        Disconnected(Cancelled).assertEquals("Disconnected(Cancelled)")
        Disconnected(ConnectionLimitReached).assertEquals("Disconnected(Connection Limit Reached)")
        Disconnected(EncryptionTimedOut).assertEquals("Disconnected(Encryption Timed Out)")
        Disconnected(Unknown(133)).assertEquals("Disconnected(133)")
        Unknown(133).assertEquals("Unknown(status=133)")
    }
}

private infix fun Any.assertEquals(expected: String) {
    assertEquals(
        expected = expected,
        actual = toString(),
    )
}
