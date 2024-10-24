package com.juul.sensortag.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
public actual fun rememberPermissionsControllerFactory(): PermissionsControllerFactory {
    return remember {
        PermissionsControllerFactory {
            object : PermissionsController {
                override suspend fun providePermission(permission: Permission) {
                    // No-op
                }

                override suspend fun isPermissionGranted(permission: Permission) = true

                override suspend fun getPermissionState(permission: Permission) =
                    PermissionState.Granted

                override fun openAppSettings() {
                    // No-op
                }
            }
        }
    }
}
