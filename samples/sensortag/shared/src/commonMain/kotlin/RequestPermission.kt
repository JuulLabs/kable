package com.juul.sensortag

import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException

suspend fun PermissionsController.requestPermission(permission: Permission) = try {
    providePermission(permission)
    PermissionState.Granted
} catch (e: DeniedAlwaysException) {
    PermissionState.DeniedAlways
} catch (e: DeniedException) {
    PermissionState.Denied
} catch (e: RequestCanceledException) {
    null
}
