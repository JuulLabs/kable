package com.juul.sensortag.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory as mokoRememberPermissionsControllerFactory

@Composable
public actual fun rememberPermissionsControllerFactory(): PermissionsControllerFactory {
    val mokoFactory = mokoRememberPermissionsControllerFactory()
    return remember {
        PermissionsControllerFactory {
            fromMoko(mokoFactory.createPermissionsController())
        }
    }
}
