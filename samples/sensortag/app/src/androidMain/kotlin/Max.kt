package com.juul.sensortag

// todo: Use Krayon provided `max` after https://github.com/JuulLabs/krayon/pull/69 is released.
public inline fun <I, O : Comparable<O>> List<I>.max(
    crossinline selector: (I) -> O?,
): O {
    var max: O? = null
    for (input in this) {
        val output = selector(input) ?: continue
        if (max == null || max < output) max = output
    }
    return checkNotNull(max)
}
