package com.juul.sensortag

// todo: Use Krayon provided `min` after https://github.com/JuulLabs/krayon/pull/69 is released.
public inline fun <I, O : Comparable<O>> List<I>.min(
    crossinline selector: (I) -> O?,
): O {
    var min: O? = null
    for (input in this) {
        val output = selector(input) ?: continue
        if (min == null || min > output) min = output
    }
    return checkNotNull(min)
}
