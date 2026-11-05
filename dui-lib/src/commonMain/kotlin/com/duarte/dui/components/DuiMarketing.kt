package com.duarte.dui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import com.duarte.dui.internal.DuiDimens
import com.duarte.dui.internal.DuiRawText
import com.duarte.dui.internal.LocalOnSurface
import com.duarte.dui.internal.duiClickable
import com.duarte.dui.internal.duiFocusRing
import com.duarte.dui.theme.DuiTheme

/**
 * A small mono label above a section title: an [index] in `accent` and the [text] in
 * `text-secondary` ("01 · Build"). Sentence case. One per section, directly above its title.
 */
@Composable
fun DuiEyebrow(text: String, modifier: Modifier = Modifier, index: String? = null) {
    val colors = DuiTheme.colors
    val label = buildAnnotatedString {
        index?.let {
            withStyle(SpanStyle(color = colors.accent)) { append(it) }
            append(" · ")
        }
        append(text)
    }
    BasicText(label, modifier, DuiTheme.typography.eyebrow.copy(color = colors.textSecondary))
}

/**
 * A card that explains one feature: [icon] in a round `surface-high` chip, a `label` [title] and a
 * `body` line. An optional mono [index] sits top-right. Lay them out in a grid of two or three.
 */
@Composable
fun DuiFeatureCard(
    title: String,
    body: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    index: String? = null,
) {
    val colors = DuiTheme.colors
    val spacing = DuiTheme.spacing
    Column(
        modifier
            .background(colors.surface, DuiTheme.shapes.lg)
            .padding(spacing.space5),
        verticalArrangement = Arrangement.spacedBy(spacing.space2),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = spacing.space3),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(DuiDimens.FeatureIconChip).background(colors.surfaceHigh, DuiTheme.shapes.full),
                contentAlignment = Alignment.Center,
            ) {
                DuiIcon(icon, contentDescription = null)
            }
            index?.let { DuiText(it, style = DuiTextStyle.Eyebrow, color = colors.textSecondary) }
        }
        DuiText(title, style = DuiTextStyle.Label)
        DuiText(body, style = DuiTextStyle.Body, color = colors.textSecondary)
    }
}

/**
 * A plain phone frame around a real app screen: a `surface-high` body with a 10 bezel, `device`
 * corners and a hairline outline. No notch, status bar or shadow.
 *
 * The [screen] is laid out at a phone's 390 x 844 and drawn at [scale] (0.6 to 0.8), so a real
 * app composable or a full-size screenshot fits without being redrawn.
 */
@Composable
fun DuiDeviceFrame(
    modifier: Modifier = Modifier,
    scale: Float = 0.8f,
    screen: @Composable () -> Unit,
) {
    require(scale > 0f) { "scale must be positive, was $scale" }
    val screenShape = RoundedCornerShape(DuiDimens.DeviceScreenRadius)
    val outer = LocalDensity.current
    Box(
        modifier
            .border(DuiDimens.Hairline, DuiTheme.colors.divider, DuiTheme.shapes.device)
            .background(DuiTheme.colors.surfaceHigh, DuiTheme.shapes.device)
            .padding(DuiDimens.DeviceBezel),
    ) {
        Box(
            Modifier
                .size(DuiDimens.DeviceScreenWidth * scale, DuiDimens.DeviceScreenHeight * scale)
                .clip(screenShape)
                .background(DuiTheme.colors.bg),
        ) {
            // A scaled density lays the screen out at full phone size and draws it smaller.
            CompositionLocalProvider(LocalDensity provides Density(outer.density * scale, outer.fontScale)) {
                Box(Modifier.size(DuiDimens.DeviceScreenWidth, DuiDimens.DeviceScreenHeight)) { screen() }
            }
        }
    }
}

/**
 * A large download button: a small [overline] over the [platform] name, with a download icon.
 * Never an official store badge. Put platforms side by side with the most common one as
 * [DuiButtonStyle.Primary]; any other style renders as the `surface` secondary.
 */
@Composable
fun DuiStoreButton(
    platform: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: DuiButtonStyle = DuiButtonStyle.Primary,
    overline: String = "Get it for",
) {
    val colors = DuiTheme.colors
    val spacing = DuiTheme.spacing
    val shape = DuiTheme.shapes.full
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    // On a surface section the secondary drops back to bg, as in the website mockup.
    val secondaryFill = when {
        pressed -> colors.surfaceHigh
        LocalOnSurface.current -> colors.bg
        else -> colors.surface
    }
    val (fill, content) = if (style == DuiButtonStyle.Primary) {
        colors.fillPrimary to colors.onPrimary
    } else {
        secondaryFill to colors.textPrimary
    }
    Row(
        modifier
            .height(DuiDimens.StoreButtonHeight)
            .pointerHoverIcon(PointerIcon.Hand)
            .duiFocusRing(source, shape)
            .duiClickable(source, Role.Button, onClick = onClick)
            .background(fill, shape)
            .padding(start = spacing.space4, end = spacing.space5),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space3),
    ) {
        DuiIcon(DuiIcons.Download, contentDescription = null, tint = content)
        Column {
            DuiRawText(overline, DuiTheme.typography.caption, content, maxLines = 1)
            DuiRawText(platform, DuiTheme.typography.label, content, maxLines = 1)
        }
    }
}
