package com.juul.sensortag.permissions

import androidx.compose.runtime.Composable

public fun interface PermissionsControllerFactory {
    public fun createPermissionsController(): PermissionsController
}

@Composable
public expect fun rememberPermissionsControllerFactory(): PermissionsControllerFactory
