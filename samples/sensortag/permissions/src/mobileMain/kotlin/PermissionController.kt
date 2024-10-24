package com.juul.sensortag.permissions

import dev.icerock.moko.permissions.PermissionsController as MokoPermissionsController

internal expect fun fromMoko(controller: MokoPermissionsController): PermissionsController
internal expect fun PermissionsController.toMoko(): MokoPermissionsController
