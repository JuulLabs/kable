package com.juul.sensortag.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

// Copied from `androidx.compose.material:material-icons-extended` library as suggested by:
// https://developer.android.com/jetpack/compose/resources#icons

val Icons.AutoMirrored.Outlined.BatteryUnknown: ImageVector by lazy {
    materialIcon(name = "AutoMirrored.Outlined.BatteryUnknown") {
        materialPath {
            moveTo(15.67f, 4.0f)
            lineTo(14.0f, 4.0f)
            lineTo(14.0f, 2.0f)
            horizontalLineToRelative(-4.0f)
            verticalLineToRelative(2.0f)
            lineTo(8.33f, 4.0f)
            curveTo(7.6f, 4.0f, 7.0f, 4.6f, 7.0f, 5.33f)
            verticalLineToRelative(15.33f)
            curveTo(7.0f, 21.4f, 7.6f, 22.0f, 8.33f, 22.0f)
            horizontalLineToRelative(7.33f)
            curveToRelative(0.74f, 0.0f, 1.34f, -0.6f, 1.34f, -1.33f)
            lineTo(17.0f, 5.33f)
            curveTo(17.0f, 4.6f, 16.4f, 4.0f, 15.67f, 4.0f)
            close()
            moveTo(13.0f, 18.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(14.3f, 12.69f)
            reflectiveCurveToRelative(-0.38f, 0.42f, -0.67f, 0.71f)
            curveToRelative(-0.48f, 0.48f, -0.83f, 1.15f, -0.83f, 1.6f)
            horizontalLineToRelative(-1.6f)
            curveToRelative(0.0f, -0.83f, 0.46f, -1.52f, 0.93f, -2.0f)
            lineToRelative(0.93f, -0.94f)
            curveToRelative(0.27f, -0.27f, 0.44f, -0.65f, 0.44f, -1.06f)
            curveToRelative(0.0f, -0.83f, -0.67f, -1.5f, -1.5f, -1.5f)
            reflectiveCurveToRelative(-1.5f, 0.67f, -1.5f, 1.5f)
            lineTo(9.0f, 11.0f)
            curveToRelative(0.0f, -1.66f, 1.34f, -3.0f, 3.0f, -3.0f)
            reflectiveCurveToRelative(3.0f, 1.34f, 3.0f, 3.0f)
            curveToRelative(0.0f, 0.66f, -0.27f, 1.26f, -0.7f, 1.69f)
            close()
        }
    }
}
