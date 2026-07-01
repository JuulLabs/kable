package dev.icerock.moko.permissions.compose

import androidx.compose.runtime.Composable

@Composable
actual fun rememberPermissionsControllerFactory(): PermissionsControllerFactory =
    PermissionsControllerFactory { NopPermissionsController }
