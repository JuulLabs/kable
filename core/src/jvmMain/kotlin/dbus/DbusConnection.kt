package com.juul.kable.dbus

import org.freedesktop.dbus.connections.impl.DBusConnection.DBusBusType.SYSTEM
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder

internal val dbus = DBusConnectionBuilder.forType(SYSTEM).build()
