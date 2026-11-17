package com.juul.sensortag.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder

val Icons.Filled.Battery0Bar: ImageVector by lazy { batteryIcon(0) }
val Icons.Filled.Battery1Bar: ImageVector by lazy { batteryIcon(1) }
val Icons.Filled.Battery2Bar: ImageVector by lazy { batteryIcon(2) }
val Icons.Filled.Battery3Bar: ImageVector by lazy { batteryIcon(3) }
val Icons.Filled.Battery4Bar: ImageVector by lazy { batteryIcon(4) }
val Icons.Filled.Battery5Bar: ImageVector by lazy { batteryIcon(5) }

private fun batteryIcon(number: Int) = materialIcon(name = "Filled.Battery${number}Bar") {
    materialPath {
        path(number)
    }
}

private val verticalLineRelativeToValues = arrayOf(14f, 12f, 10f, 8f, 6f, 4f)

// Adapted from `androidx.compose.material:material-icons-extended` library as suggested by:
// https://developer.android.com/jetpack/compose/resources#icons
private fun PathBuilder.path(number: Int) {
    moveTo(17.0f, 5.0f)
    verticalLineToRelative(16.0f)
    curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
    horizontalLineTo(8.0f)
    curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
    verticalLineTo(5.0f)
    curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
    horizontalLineToRelative(2.0f)
    verticalLineTo(2.0f)
    horizontalLineToRelative(4.0f)
    verticalLineToRelative(2.0f)
    horizontalLineToRelative(2.0f)
    curveTo(16.55f, 4.0f, 17.0f, 4.45f, 17.0f, 5.0f)
    close()
    moveTo(15.0f, 6.0f)
    horizontalLineTo(9.0f)
    verticalLineToRelative(verticalLineRelativeToValues[number])
    horizontalLineToRelative(6.0f)
    verticalLineTo(6.0f)
    close()
}
