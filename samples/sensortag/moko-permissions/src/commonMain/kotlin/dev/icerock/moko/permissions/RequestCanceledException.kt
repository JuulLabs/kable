/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.permissions

class RequestCanceledException(
    val permission: Permission,
    message: String? = null
) : Exception(message)
