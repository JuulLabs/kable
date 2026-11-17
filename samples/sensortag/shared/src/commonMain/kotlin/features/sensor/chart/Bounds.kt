package com.juul.sensortag.features.sensor.chart

class Bounds(
    var top: Float = 0f,
    var bottom: Float = 0f,
    var left: Float = 0f,
    var right: Float = 0f,
) {

    fun set(
        top: Float,
        bottom: Float,
        left: Float,
        right: Float,
    ): Bounds {
        this.top = top
        this.bottom = bottom
        this.left = left
        this.right = right
        return this
    }

    fun inset(
        top: Float,
        bottom: Float,
        left: Float,
        right: Float,
    ): Bounds {
        this.top += top
        this.bottom -= bottom
        this.left += left
        this.right -= right
        return this
    }
}

fun Bounds.set(width: Float, height: Float): Bounds =
    set(top = 0f, bottom = height, left = 0f, right = width)

fun Bounds.inset(bounds: Bounds): Bounds =
    inset(bounds.top, bounds.bottom, bounds.left, bounds.right)

val Bounds.width: Float
    get() = right - left

val Bounds.height: Float
    get() = top - bottom

val Bounds.x: List<Float>
    get() = listOf(left, right)

val Bounds.y: List<Float>
    get() = listOf(top, bottom)
