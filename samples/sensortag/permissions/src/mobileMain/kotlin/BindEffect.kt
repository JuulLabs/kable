package com.juul.sensortag.permissions

import androidx.compose.runtime.Composable
import dev.icerock.moko.permissions.compose.BindEffect as MokoBindEffect

@Composable
public actual fun BindEffect(permissionsController: PermissionsController) {
    MokoBindEffect(permissionsController.toMoko())
}
