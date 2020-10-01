package com.juul.kable.external

/**
 * Per Web IDL:.
 *
 * ```
 * typedef (ArrayBufferView or ArrayBuffer) BufferSource;
 * typedef (Int8Array or Int16Array or Int32Array or
 *          Uint8Array or Uint16Array or Uint32Array or Uint8ClampedArray or
 *          Float32Array or Float64Array or DataView) ArrayBufferView;
 * ```
 *
 * [kotlin.ByteArray] are mapped to JavaScript `Int8Array`; therefore we can use [ByteArray] where external Javascript
 * expects a [BufferSource].
 *
 * - [BufferSource](https://heycam.github.io/webidl/#BufferSource)
 * - [ArrayBufferView](https://heycam.github.io/webidl/#ArrayBufferView)
 * - [Representing Kotlin types in JavaScript](https://kotlinlang.org/docs/reference/js-to-kotlin-interop.html#representing-kotlin-types-in-javascript)
 */
internal typealias BufferSource = ByteArray
