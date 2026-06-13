package com.juul.sensortag.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

// Copied from `androidx.compose.material:material-icons-extended` library as suggested by:
// https://developer.android.com/jetpack/compose/resources#icons

val Icons.Filled.BatteryFull: ImageVector by lazy {
    materialIcon(name = "Filled.BatteryFull") {
        materialPath {
            moveTo(15.67f, 4.0f)
            horizontalLineTo(14.0f)
            verticalLineTo(2.0f)
            horizontalLineToRelative(-4.0f)
            verticalLineToRelative(2.0f)
            horizontalLineTo(8.33f)
            curveTo(7.6f, 4.0f, 7.0f, 4.6f, 7.0f, 5.33f)
            verticalLineToRelative(15.33f)
            curveTo(7.0f, 21.4f, 7.6f, 22.0f, 8.33f, 22.0f)
            horizontalLineToRelative(7.33f)
            curveToRelative(0.74f, 0.0f, 1.34f, -0.6f, 1.34f, -1.33f)
            verticalLineTo(5.33f)
            curveTo(17.0f, 4.6f, 16.4f, 4.0f, 15.67f, 4.0f)
            close()
        }
    }
}
