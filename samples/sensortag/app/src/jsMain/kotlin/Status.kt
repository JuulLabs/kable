package com.juul.sensortag

typealias StatusListener = (message: String) -> Unit

@JsExport
class Status {

    private val listeners = mutableListOf<StatusListener>()

    internal fun emit(status: String) {
        listeners.forEach { listener -> listener.invoke(status) }
    }

    fun addListener(listener: StatusListener) { listeners += listener }
    fun removeListener(listener: StatusListener) { listeners -= listener }
}
