package com.duarte.dui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.duarte.dui.internal.DuiDimens
import com.duarte.dui.internal.duiClickable
import com.duarte.dui.internal.duiFocusRing
import com.duarte.dui.theme.DuiTheme

/**
 * Screen container on `bg`. Pads for the system bars: the top and sides always, the bottom only
 * when there is no [bottomBar] (the bar pads itself).
 */
@Composable
fun DuiScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val sides = if (bottomBar == null) {
        WindowInsetsSides.Top + WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
    } else {
        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
    }
    Column(
        modifier
            .fillMaxSize()
            .background(DuiTheme.colors.bg)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(sides)),
    ) {
        topBar()
        Box(Modifier.weight(1f).fillMaxWidth()) { content(PaddingValues()) }
        bottomBar?.invoke()
    }
}

/**
 * Top bar with an optional back action (icon plus visible [backLabel]), an optional `accent` text
 * action and an optional [trailing] caption.
 */
@Composable
fun DuiTopBar(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backLabel: String = "Back",
    actionText: String? = null,
    onAction: () -> Unit = {},
    trailing: String? = null,
) {
    val spacing = DuiTheme.spacing
    Row(
        modifier.fillMaxWidth().heightIn(min = spacing.space7).padding(horizontal = spacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            val source = remember { MutableInteractionSource() }
            Row(
                Modifier
                    .heightIn(min = spacing.space7)
                    .duiFocusRing(source, DuiTheme.shapes.sm)
                    .duiClickable(source, Role.Button, onClick = onBack),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space1),
            ) {
                DuiIcon(DuiIcons.Back, contentDescription = null)
                DuiText(backLabel, style = DuiTextStyle.Label, maxLines = 1)
            }
        }
        Box(Modifier.weight(1f))
        if (trailing != null) DuiText(trailing, style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary, maxLines = 1)
        if (actionText != null) {
            val source = remember { MutableInteractionSource() }
            DuiText(
                actionText,
                Modifier
                    .heightIn(min = spacing.space7)
                    .duiFocusRing(source, DuiTheme.shapes.sm)
                    .duiClickable(source, Role.Button, onClick = onAction)
                    .padding(vertical = spacing.space3),
                style = DuiTextStyle.Label,
                color = DuiTheme.colors.accent,
                maxLines = 1,
            )
        }
    }
}

enum class DuiCardTone {
    /** `surface`: one step off `bg`. */
    Surface,

    /** `accentSoft`: a quiet accent wash. Pair with `accent` text. */
    Accent,
}

/** Flat container: no border and no shadow. Clickable when [onClick] is set. */
@Composable
fun DuiCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    tone: DuiCardTone = DuiCardTone.Surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = DuiTheme.colors
    val shape = DuiTheme.shapes.lg
    val source = remember { MutableInteractionSource() }
    val click = if (onClick != null) {
        Modifier.duiFocusRing(source, shape).duiClickable(source, Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Column(
        modifier
            .fillMaxWidth()
            .then(click)
            .background(if (tone == DuiCardTone.Surface) colors.surface else colors.accentSoft, shape)
            .padding(DuiTheme.spacing.space4),
        content = content,
    )
}

/** A 1px `divider` hairline. */
@Composable
fun DuiDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().heightIn(min = DuiDimens.Hairline, max = DuiDimens.Hairline).background(DuiTheme.colors.divider))
}

/** A quiet block for "nothing here yet" and failure states, with an optional action. */
@Composable
fun DuiEmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    actionText: String? = null,
    onAction: () -> Unit = {},
    actionStyle: DuiButtonStyle = DuiButtonStyle.Primary,
) {
    val spacing = DuiTheme.spacing
    Column(
        modifier.fillMaxWidth().background(DuiTheme.colors.surface, DuiTheme.shapes.lg).padding(spacing.space5),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.space2),
    ) {
        if (icon != null) DuiIcon(icon, contentDescription = null, tint = DuiTheme.colors.accent)
        DuiText(title, style = DuiTextStyle.Title, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        DuiText(body, style = DuiTextStyle.Body, color = DuiTheme.colors.textSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (actionText != null) {
            Box(Modifier.padding(top = spacing.space3)) { DuiButton(actionText, onAction, style = actionStyle) }
        }
    }
}
