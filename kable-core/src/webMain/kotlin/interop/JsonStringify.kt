package com.juul.kable.interop

import kotlin.js.JsAny
import kotlin.js.JsString
import kotlin.js.js

internal fun jsonStringify(obj: JsAny?): String =
    jsonJsStringify(obj).toString()

internal fun jsonJsStringify(obj: JsAny?): JsString =
    js("JSON.stringify(obj)")
