package com.juul.sensortag

typealias MovementListener = (x: Float, y: Float, z: Float) -> Unit

@JsExport
class Movement {

    private val listeners = mutableListOf<MovementListener>()

    internal fun emit(movement: Vector3f) {
        val (x, y, z) = movement
        listeners.forEach { listener -> listener.invoke(x, y, z) }
    }

    fun addListener(listener: MovementListener) { listeners += listener }
    fun removeListener(listener: MovementListener) { listeners -= listener }
}
