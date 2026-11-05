package com.duarte.dui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.duarte.dui.internal.DuiDimens
import com.duarte.dui.theme.DuiTheme

/** Draws [icon] tinted with [tint]. Pass a null [contentDescription] when a label sits next to it. */
@Composable
fun DuiIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = DuiTheme.colors.textPrimary,
    size: Dp = DuiDimens.IconSize,
) {
    Image(
        rememberVectorPainter(icon),
        contentDescription,
        modifier.size(size),
        colorFilter = ColorFilter.tint(tint),
    )
}

/** Outline icons on a 24 grid with a 1.5 round stroke. They take their colour from [DuiIcon]. */
object DuiIcons {
    val Home by lazy { icon("home", "M4 11l8-7 8 7v9H4z") }
    val History by lazy { icon("history", "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0", "M12 7v5l3 2") }
    val Settings by lazy {
        icon(
            "settings",
            "M4 7h10", "M18 7h2", "M4 17h2", "M10 17h10",
            "M14 7a2 2 0 1 0 4 0a2 2 0 1 0 -4 0", "M6 17a2 2 0 1 0 4 0a2 2 0 1 0 -4 0",
        )
    }
    val Sound by lazy {
        icon("sound", "M9 18V6l10-2v12", "M3 18a3 3 0 1 0 6 0a3 3 0 1 0 -6 0", "M13 16a3 3 0 1 0 6 0a3 3 0 1 0 -6 0")
    }
    val Vibration by lazy { icon("vibration", "M9 4h6v16H9z", "M5 8v8", "M19 8v8") }
    val Back by lazy { icon("back", "M15 5l-7 7 7 7") }
    val Plus by lazy { icon("plus", "M12 5v14", "M5 12h14") }
    val Minus by lazy { icon("minus", "M5 12h14") }
    val Check by lazy { icon("check", "M5 12l5 5 9-10") }
    val Close by lazy { icon("close", "M6 6l12 12", "M18 6L6 18") }
    val More by lazy { icon("more", "M5 12h.01", "M12 12h.01", "M19 12h.01") }
    val ArrowUp by lazy { icon("arrow-up", "M12 19V5", "M6 11l6-6 6 6") }
    val ArrowDown by lazy { icon("arrow-down", "M12 5v14", "M6 13l6 6 6-6") }
    val TrendUp by lazy { icon("trend-up", "M4 18l5-6 4 3 7-9") }
    val Sets by lazy { icon("sets", "M4 7h16", "M4 12h16", "M4 17h16") }
    val Download by lazy { icon("download", "M12 4v12", "M7 11l5 5 5-5", "M5 20h14") }
    val Search by lazy { icon("search", "M11 4a7 7 0 1 0 0 14a7 7 0 1 0 0 -14", "M16 16l4 4") }
    val Speaker by lazy { icon("speaker", "M5 9v6h3l5 4V5L8 9z", "M17 9a4 4 0 0 1 0 6") }
    val Contrast by lazy {
        icon("contrast", "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0", "M12 3v18", "M12 8l5-4", "M12 13l8-6", "M12 18l8-7")
    }
    val Spark by lazy { icon("spark", "M12 4v16", "M4 12h16", "M6.5 6.5l11 11", "M17.5 6.5l-11 11") }

    private fun icon(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            paths.forEach {
                addPath(
                    PathParser().parsePathString(it).toNodes(),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = DuiDimens.IconStroke.value,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()
}
