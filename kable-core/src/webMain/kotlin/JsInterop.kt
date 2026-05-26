package com.juul.kable

import kotlin.js.JsAny
import kotlin.js.js

/** JavaScript `undefined` for equality checks where values are typed as [JsAny]. */
internal val jsUndefinedAny: JsAny? = js("undefined")
