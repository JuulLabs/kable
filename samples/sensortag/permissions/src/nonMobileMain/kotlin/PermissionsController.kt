package com.juul.sensortag.permissions

public actual interface PermissionsController {
    public actual suspend fun providePermission(permission: Permission)
    public actual suspend fun isPermissionGranted(permission: Permission): Boolean
    public actual suspend fun getPermissionState(permission: Permission): PermissionState
    public actual fun openAppSettings()
}
