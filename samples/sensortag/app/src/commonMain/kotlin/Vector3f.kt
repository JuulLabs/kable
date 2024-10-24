package com.juul.sensortag

data class Vector3f(val x: Float, val y: Float, val z: Float)

fun Vector3f(data: ByteArray) = Vector3f(
    x = data.readShort(0).toFloat(),
    y = data.readShort(2).toFloat(),
    z = data.readShort(4).toFloat(),
)

operator fun Vector3f.times(scalar: Float) = Vector3f(x * scalar, y * scalar, z * scalar)
