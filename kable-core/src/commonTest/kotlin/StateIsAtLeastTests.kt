package com.juul.kable

import kotlinx.coroutines.GlobalScope
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public class StateIsAtLeastTests {

    @Test
    fun disconnected_isAtLeast_disconnected_is_true() {
        assertTrue(State.Disconnected().isAtLeast<State.Disconnected>())
    }

    @Test
    fun disconnected_isAtLeast_disconnecting_is_false() {
        assertFalse(State.Disconnected().isAtLeast<State.Disconnecting>())
    }

    @Test
    fun disconnected_isAtLeast_connectingBluetooth_is_false() {
        assertFalse(State.Disconnected().isAtLeast<State.Connecting.Bluetooth>())
    }

    @Test
    fun disconnected_isAtLeast_connectingServices_is_false() {
        assertFalse(State.Disconnected().isAtLeast<State.Connecting.Services>())
    }

    @Test
    fun disconnected_isAtLeast_connectingObserves_is_false() {
        assertFalse(State.Disconnected().isAtLeast<State.Connecting.Observes>())
    }

    @Test
    fun disconnected_isAtLeast_connected_is_false() {
        assertFalse(State.Disconnected().isAtLeast<State.Connected>())
    }

    @Test
    fun disconnecting_isAtLeast_disconnected_is_true() {
        assertTrue(State.Disconnecting.isAtLeast<State.Disconnected>())
    }

    @Test
    fun disconnecting_isAtLeast_disconnecting_is_true() {
        assertTrue(State.Disconnecting.isAtLeast<State.Disconnecting>())
    }

    @Test
    fun disconnecting_isAtLeast_connectingBluetooth_is_false() {
        assertFalse(State.Disconnecting.isAtLeast<State.Connecting.Bluetooth>())
    }

    @Test
    fun disconnecting_isAtLeast_connectingServices_is_false() {
        assertFalse(State.Disconnecting.isAtLeast<State.Connecting.Services>())
    }

    @Test
    fun disconnecting_isAtLeast_connectingObserves_is_false() {
        assertFalse(State.Disconnecting.isAtLeast<State.Connecting.Observes>())
    }

    @Test
    fun disconnecting_isAtLeast_connected_is_false() {
        assertFalse(State.Disconnecting.isAtLeast<State.Connected>())
    }

    @Test
    fun connectingBluetooth_isAtLeast_disconnected_is_true() {
        assertTrue(State.Connecting.Bluetooth.isAtLeast<State.Disconnected>())
    }

    @Test
    fun connectingBluetooth_isAtLeast_disconnecting_is_true() {
        assertTrue(State.Connecting.Bluetooth.isAtLeast<State.Disconnecting>())
    }

    @Test
    fun connectingBluetooth_isAtLeast_connectingBluetooth_is_true() {
        assertTrue(State.Connecting.Bluetooth.isAtLeast<State.Connecting.Bluetooth>())
    }

    @Test
    fun connectingBluetooth_isAtLeast_connectingServices_is_false() {
        assertFalse(State.Connecting.Bluetooth.isAtLeast<State.Connecting.Services>())
    }

    @Test
    fun connectingBluetooth_isAtLeast_connectingObserves_is_false() {
        assertFalse(State.Connecting.Bluetooth.isAtLeast<State.Connecting.Observes>())
    }

    @Test
    fun connectingBluetooth_isAtLeast_connected_is_false() {
        assertFalse(State.Connecting.Bluetooth.isAtLeast<State.Connected>())
    }

    @Test
    fun connectingServices_isAtLeast_disconnected_is_true() {
        assertTrue(State.Connecting.Services.isAtLeast<State.Disconnected>())
    }

    @Test
    fun connectingServices_isAtLeast_disconnecting_is_true() {
        assertTrue(State.Connecting.Services.isAtLeast<State.Disconnecting>())
    }

    @Test
    fun connectingServices_isAtLeast_connectingBluetooth_is_true() {
        assertTrue(State.Connecting.Services.isAtLeast<State.Connecting.Bluetooth>())
    }

    @Test
    fun connectingServices_isAtLeast_connectingServices_is_true() {
        assertTrue(State.Connecting.Services.isAtLeast<State.Connecting.Services>())
    }

    @Test
    fun connectingServices_isAtLeast_connectingObserves_is_false() {
        assertFalse(State.Connecting.Services.isAtLeast<State.Connecting.Observes>())
    }

    @Test
    fun connectingServices_isAtLeast_connected_is_false() {
        assertFalse(State.Connecting.Services.isAtLeast<State.Connected>())
    }

    @Test
    fun connectingObserves_isAtLeast_disconnected_is_true() {
        assertTrue(State.Connecting.Observes.isAtLeast<State.Disconnected>())
    }

    @Test
    fun connectingObserves_isAtLeast_disconnecting_is_true() {
        assertTrue(State.Connecting.Observes.isAtLeast<State.Disconnecting>())
    }

    @Test
    fun connectingObserves_isAtLeast_connectingBluetooth_is_true() {
        assertTrue(State.Connecting.Observes.isAtLeast<State.Connecting.Bluetooth>())
    }

    @Test
    fun connectingObserves_isAtLeast_connectingServices_is_true() {
        assertTrue(State.Connecting.Observes.isAtLeast<State.Connecting.Services>())
    }

    @Test
    fun connectingObserves_isAtLeast_connectingObserves_is_true() {
        assertTrue(State.Connecting.Observes.isAtLeast<State.Connecting.Observes>())
    }

    @Test
    fun connectingObserves_isAtLeast_connected_is_false() {
        assertFalse(State.Connecting.Observes.isAtLeast<State.Connected>())
    }

    @Test
    fun connected_isAtLeast_disconnected_is_true() {
        assertTrue(State.Connected(GlobalScope).isAtLeast<State.Disconnected>())
    }

    @Test
    fun connected_isAtLeast_disconnecting_is_true() {
        assertTrue(State.Connected(GlobalScope).isAtLeast<State.Disconnecting>())
    }

    @Test
    fun connected_isAtLeast_connectingBluetooth_is_true() {
        assertTrue(State.Connected(GlobalScope).isAtLeast<State.Connecting.Bluetooth>())
    }

    @Test
    fun connected_isAtLeast_connectingServices_is_true() {
        assertTrue(State.Connected(GlobalScope).isAtLeast<State.Connecting.Services>())
    }

    @Test
    fun connected_isAtLeast_connectingObserves_is_true() {
        assertTrue(State.Connected(GlobalScope).isAtLeast<State.Connecting.Observes>())
    }

    @Test
    fun connected_isAtLeast_connected_is_true() {
        assertTrue(State.Connected(GlobalScope).isAtLeast<State.Connected>())
    }
}
