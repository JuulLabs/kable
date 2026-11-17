package dev.icerock.moko.permissions.compose

import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionState.Granted
import dev.icerock.moko.permissions.PermissionsController

internal object NopPermissionsController : PermissionsController {

    override suspend fun providePermission(permission: Permission) {
        // No-op
    }

    override suspend fun isPermissionGranted(permission: Permission): Boolean = true

    override suspend fun getPermissionState(permission: Permission): PermissionState = Granted

    override fun openAppSettings() {
        // No-op
    }
}
