package com.juul.kable.interop

import kotlin.js.JsArray
import kotlin.js.length

internal fun JsArray<*>.isEmpty() = length == 0
internal fun JsArray<*>.isNotEmpty() = !isEmpty()
