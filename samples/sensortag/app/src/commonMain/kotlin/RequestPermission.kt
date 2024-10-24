package com.juul.sensortag

import com.juul.sensortag.permissions.DeniedAlwaysException
import com.juul.sensortag.permissions.DeniedException
import com.juul.sensortag.permissions.Permission
import com.juul.sensortag.permissions.PermissionState
import com.juul.sensortag.permissions.PermissionsController

suspend fun PermissionsController.requestPermission(permission: Permission) = try {
    providePermission(permission)
    PermissionState.Granted
} catch (e: DeniedAlwaysException) {
    PermissionState.DeniedAlways
} catch (e: DeniedException) {
    PermissionState.Denied
} catch (e: Exception) {
    // RequestCanceledException
    null
}
