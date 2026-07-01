/*
 * Copyright 2021 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.permissions

import android.content.Context
import androidx.activity.ComponentActivity

actual interface PermissionsController {
    actual suspend fun providePermission(permission: Permission)
    actual suspend fun isPermissionGranted(permission: Permission): Boolean
    actual suspend fun getPermissionState(permission: Permission): PermissionState
    actual fun openAppSettings()

    fun bind(activity: ComponentActivity)

    companion object {
        operator fun invoke(
            applicationContext: Context
        ): PermissionsController {
            return PermissionsControllerImpl(
                applicationContext = applicationContext
            )
        }
    }
}
