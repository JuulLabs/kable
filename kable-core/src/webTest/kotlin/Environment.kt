package com.juul.kable

import kotlin.js.JsBoolean
import kotlin.js.js

val isBrowser: JsBoolean = js("typeof window !== 'undefined'")

val isNode: JsBoolean = js("typeof process !== 'undefined' && process.versions && process.versions.node")
