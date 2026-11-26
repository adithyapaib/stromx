package com.adithyapaib.stromx.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** A circle as SVG path data, for icons drawn from path strings only. */
private fun circle(cx: Float, cy: Float, r: Float) =
    "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

/** One consistent outline set on a 24-unit grid; tinted by `Icon(tint = …)`. */
private fun strokeIcon(name: String, vararg paths: String, width: Float = 2f): ImageVector =
    ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .apply {
            paths.forEach { d ->
                addPath(
                    pathData = PathParser().parsePathString(d).toNodes(),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = width,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                )
            }
        }
        .build()

object StromXIcons {
    val Gauge = strokeIcon("gauge", "M4 16a8 8 0 1 1 16 0", "M12 16l4-5", circle(12f, 16f, 1.3f))
    val Route = strokeIcon(
        "route",
        circle(6f, 18f, 2.5f),
        circle(18f, 6f, 2.5f),
        "M8.5 18H15a3 3 0 0 0 0-6H9a3 3 0 0 1 0-6h6.5"
    )
    val Wrench = strokeIcon("wrench", "M14.7 6.3a4 4 0 0 0-5.4 5.4L3.5 17.5a1.5 1.5 0 0 0 2.1 2.1l5.8-5.8a4 4 0 0 0 5.4-5.4l-2.4 2.4-2.1-.4-.4-2.1z")
    val Gear = strokeIcon(
        "gear",
        circle(12f, 12f, 3f),
        "M12 2v3M12 19v3M4.9 4.9l2.1 2.1M17 17l2.1 2.1M2 12h3M19 12h3M4.9 19.1L7 17M17 7l2.1-2.1"
    )
    val Fuel = strokeIcon(
        "fuel",
        "M4 20V5a1 1 0 0 1 1-1h8a1 1 0 0 1 1 1v15",
        "M3 20h12",
        "M6 8h6",
        "M14 10h2a2 2 0 0 1 2 2v4a1.5 1.5 0 0 0 3 0V8l-3-3"
    )
    val Thermometer = strokeIcon("thermometer", "M14 14.8V5a2 2 0 0 0-4 0v9.8a4 4 0 1 0 4 0z")
    val Flag = strokeIcon("flag", "M5 21V4", "M5 4h11l-2 4 2 4H5")
    val Chevron = strokeIcon("chevron", "M9 6l6 6-6 6", width = 2.6f)
    val Back = strokeIcon("back", "M15 6l-6 6 6 6", width = 2.6f)
    val Plus = strokeIcon("plus", "M12 5v14M5 12h14", width = 2.4f)
    val Bluetooth = strokeIcon("bluetooth", "M7 7l10 10-5 4V3l5 4L7 17")
    val TurnRight = strokeIcon("turn", "M5 20v-6a4 4 0 0 1 4-4h10", "M15 6l4 4-4 4", width = 1.8f)
    val Bike = strokeIcon(
        "bike",
        circle(5.5f, 16f, 3.5f),
        circle(18.5f, 16f, 3.5f),
        "M5.5 16l4-6h5l4 6",
        "M13 6h3l1.5 4"
    )
}
