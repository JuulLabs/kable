package com.juul.sensortag.permissions

import dev.icerock.moko.permissions.PermissionsController as MokoPermissionsController

public actual interface PermissionsController {
    public actual suspend fun providePermission(permission: Permission)
    public actual suspend fun isPermissionGranted(permission: Permission): Boolean
    public actual suspend fun getPermissionState(permission: Permission): PermissionState
    public actual fun openAppSettings()
}

internal actual fun fromMoko(controller: MokoPermissionsController): PermissionsController =
    DelegatingPermissionsController(controller)

private class DelegatingPermissionsController(
    private val delegate: MokoPermissionsController,
) : PermissionsController {
    override suspend fun providePermission(permission: Permission) =
        delegate.providePermission(permission)

    override suspend fun isPermissionGranted(permission: Permission) =
        delegate.isPermissionGranted(permission)

    override suspend fun getPermissionState(permission: Permission) =
        delegate.getPermissionState(permission)

    override fun openAppSettings() {
        delegate.openAppSettings()
    }
}

internal actual fun PermissionsController.toMoko(): MokoPermissionsController =
    DelegatingMokoPermissionsController(this)

private class DelegatingMokoPermissionsController(
    private val delegate: PermissionsController,
) : MokoPermissionsController {
    override suspend fun providePermission(permission: Permission) =
        delegate.providePermission(permission)

    override suspend fun isPermissionGranted(permission: Permission) =
        delegate.isPermissionGranted(permission)

    override suspend fun getPermissionState(permission: Permission) =
        delegate.getPermissionState(permission)

    override fun openAppSettings() {
        delegate.openAppSettings()
    }
}
