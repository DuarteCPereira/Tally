package com.duarte.dui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.duarte.dui.theme.DuiTheme

/**
 * The Tally app icon: four tally strokes crossed by a barbell, `on-primary` on a `fill-primary`
 * rounded square, so it inverts between themes. It is black and white only, never tinted.
 *
 * With [animate] the strokes draw in one by one, then the barbell fades in, once. Callers must pass
 * `animate = false` when the user prefers reduced motion; the library cannot see that setting on
 * every platform.
 *
 * Sizes in use: 24 (footer), 32 (site header), 72 (hero).
 */
@Composable
fun DuiTallyMark(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    animate: Boolean = false,
) {
    val progress = remember { Animatable(if (animate) 0f else TotalMillis.toFloat()) }
    LaunchedEffect(animate) {
        if (animate) progress.animateTo(TotalMillis.toFloat(), tween(TotalMillis, easing = LinearEasing))
    }
    val tile = DuiTheme.colors.fillPrimary
    val ink = DuiTheme.colors.onPrimary
    Canvas(modifier.size(size)) {
        // Draw in the 48-unit grid the mark was designed on.
        scale(this.size.width / Grid, this.size.height / Grid, pivot = Offset.Zero) {
            drawRoundRect(tile, size = Size(Grid, Grid), cornerRadius = CornerRadius(TileRadius))
            drawStrokes(ink, elapsedMillis = progress.value)
            drawBarbell(tile, ink, alpha = barbellAlpha(progress.value))
        }
    }
}

private const val Grid = 48f
private const val TileRadius = 11f
private const val InkWidth = 3f
private val StrokeXs = listOf(15f, 21f, 27f, 33f)
private const val StrokeTop = 13f
private const val StrokeBottom = 35f
private const val BarbellAngle = -25f
private const val BarbellCutout = 7f
private const val PlateCutout = 2.5f
private const val PlateRadius = 1.5f

/** Animation timeline, matching the website mockup: 0.5s per stroke, 0.1s apart, then the barbell. */
private const val StrokeMillis = 500
private const val StrokeStaggerMillis = 100
private const val BarbellDelayMillis = 500
private const val BarbellMillis = 350
private const val TotalMillis = BarbellDelayMillis + BarbellMillis
private val DrawEasing = CubicBezierEasing(0.2f, 0.7f, 0.2f, 1f)

private fun DrawScope.drawStrokes(ink: Color, elapsedMillis: Float) {
    StrokeXs.forEachIndexed { index, x ->
        val local = ((elapsedMillis - index * StrokeStaggerMillis) / StrokeMillis).coerceIn(0f, 1f)
        val fraction = DrawEasing.transform(local)
        if (fraction <= 0f) return@forEachIndexed
        val end = StrokeTop + (StrokeBottom - StrokeTop) * fraction
        drawLine(ink, Offset(x, StrokeTop), Offset(x, end), strokeWidth = InkWidth, cap = StrokeCap.Round)
    }
}

private fun barbellAlpha(elapsedMillis: Float): Float =
    ((elapsedMillis - BarbellDelayMillis) / BarbellMillis).coerceIn(0f, 1f)

/** The bar is cut out of the strokes behind it with a [tile]-coloured outline, so the two never merge. */
private fun DrawScope.drawBarbell(tile: Color, ink: Color, alpha: Float) {
    if (alpha <= 0f) return
    val center = Offset(Grid / 2, Grid / 2)
    rotate(BarbellAngle, pivot = center) {
        val start = Offset(8f, Grid / 2)
        val end = Offset(40f, Grid / 2)
        drawLine(tile, start, end, strokeWidth = BarbellCutout, cap = StrokeCap.Round, alpha = alpha)
        drawLine(ink, start, end, strokeWidth = InkWidth, cap = StrokeCap.Round, alpha = alpha)
        listOf(10f, 34f).forEach { x ->
            val topLeft = Offset(x, 17f)
            val plate = Size(4f, 14f)
            val corner = CornerRadius(PlateRadius)
            drawRoundRect(tile, topLeft, plate, corner, style = Stroke(PlateCutout), alpha = alpha)
            drawRoundRect(ink, topLeft, plate, corner, alpha = alpha)
        }
    }
}
