package com.juul.kable.interop

import js.objects.unsafeJso
import kotlin.js.JsAny
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener

private external interface MutableEventListener : JsAny {
    var handleEvent: (Event) -> Unit
}

internal fun EventListener(callback: (Event) -> Unit): EventListener {
    val value = unsafeJso<MutableEventListener>()
    value.handleEvent = callback
    return value as EventListener
}
