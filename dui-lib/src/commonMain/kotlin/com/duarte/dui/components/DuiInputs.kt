package com.duarte.dui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import com.duarte.dui.internal.LocalOnSurface
import com.duarte.dui.internal.duiFocusRing
import com.duarte.dui.theme.DuiTheme

/**
 * Text input on `surface` with a `caption` label above it. Focus shows the 2px `focus` ring.
 * [error] is shown under the field in `negative`; the app supplies the message.
 */
@Composable
fun DuiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    error: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val colors = DuiTheme.colors
    val spacing = DuiTheme.spacing
    val shape = DuiTheme.shapes.md
    val source = remember { MutableInteractionSource() }
    val fill = if (LocalOnSurface.current) colors.surfaceHigh else colors.surface
    Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
        if (label != null) DuiText(label, style = DuiTextStyle.Caption, color = colors.textSecondary)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .duiFocusRing(source, shape)
                .background(fill, shape)
                .heightIn(min = spacing.space7)
                .semantics { if (error != null) error(error) },
            textStyle = DuiTheme.typography.body.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.accent),
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = keyboardOptions,
            interactionSource = source,
            decorationBox = { inner ->
                Box(Modifier.padding(horizontal = spacing.space4, vertical = spacing.space3), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty() && placeholder != null) {
                        DuiText(placeholder, style = DuiTextStyle.Body, color = colors.textSecondary, maxLines = 1)
                    }
                    inner()
                }
            },
        )
        if (error != null) DuiText(error, style = DuiTextStyle.Caption, color = colors.negative)
    }
}

/** A photo on `surface`, cropped to a 16:9 card. Shows [placeholderIcon] when [image] is null. */
@Composable
fun DuiImageCard(
    image: ImageBitmap?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    placeholderIcon: ImageVector? = null,
) {
    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(DuiTheme.shapes.lg)
            .background(DuiTheme.colors.surface),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(image, contentDescription, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else if (placeholderIcon != null) {
            DuiIcon(placeholderIcon, contentDescription = null, tint = DuiTheme.colors.textSecondary)
        }
    }
}
