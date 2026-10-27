package com.duarte.dui.internal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.duarte.dui.theme.DuiTheme

/** Draws the 2dp `focus` ring, 2dp outside [shape], while [source] is focused. */
@Composable
internal fun Modifier.duiFocusRing(source: InteractionSource, shape: Shape): Modifier {
    val focused by source.collectIsFocusedAsState()
    if (!focused) return this
    val color = DuiTheme.colors.focus
    return drawWithContent {
        drawContent()
        val width = DuiDimens.FocusRingWidth.toPx()
        val inset = DuiDimens.FocusRingOffset.toPx() + width / 2
        val outline = shape.createOutline(Size(size.width + inset * 2, size.height + inset * 2), layoutDirection, this)
        translate(-inset, -inset) { drawOutline(outline, color, style = Stroke(width)) }
    }
}

/** Clickable without the platform ripple: DUI components show pressed state with tokens instead. */
internal fun Modifier.duiClickable(
    source: MutableInteractionSource,
    role: Role,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = clickable(interactionSource = source, indication = null, enabled = enabled, role = role, onClick = onClick)

@Composable
internal fun DuiRawText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    BasicText(text, modifier, style.copy(color = color), overflow = TextOverflow.Ellipsis, maxLines = maxLines)
}
