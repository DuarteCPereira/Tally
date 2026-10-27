package com.duarte.dui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.duarte.dui.internal.LocalOnSurface
import com.duarte.dui.internal.duiClickable
import com.duarte.dui.internal.duiFocusRing
import com.duarte.dui.theme.DuiTheme

/**
 * Modal dialog: `surface` card on a `scrim`, a `title`, scrollable [content] and stacked buttons.
 * Pass [DuiButton]s for [confirmButton] (usually Primary) and [dismissButton] (Ghost).
 * With [dismissible] false, back and outside taps do nothing.
 */
@Composable
fun DuiDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    dismissible: Boolean = true,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val spacing = DuiTheme.spacing
    val outside = remember { MutableInteractionSource() }
    val inside = remember { MutableInteractionSource() }
    Dialog(
        onDismissRequest = { if (dismissible) onDismissRequest() },
        properties = DialogProperties(
            dismissOnBackPress = dismissible,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(DuiTheme.colors.scrim)
                .duiClickable(outside, Role.Button, enabled = dismissible, onClick = onDismissRequest)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(spacing.space4),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .heightIn(max = 640.dp)
                    .background(DuiTheme.colors.surface, DuiTheme.shapes.lg)
                    .duiClickable(inside, Role.Button, onClick = {})
                    .padding(spacing.space5),
                verticalArrangement = Arrangement.spacedBy(spacing.space3),
            ) {
                DuiText(title, style = DuiTextStyle.Title)
                CompositionLocalProvider(LocalOnSurface provides true) {
                    Column(
                        Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(spacing.space3),
                        content = content,
                    )
                    Column(Modifier.padding(top = spacing.space2), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                        confirmButton()
                        dismissButton?.invoke()
                    }
                }
            }
        }
    }
}

@Immutable
data class DuiMenuItem(val label: String, val onClick: () -> Unit)

/** A small menu on `surface-high`, shown below [anchor] while [expanded]. Items are plain `label` text. */
@Composable
fun DuiMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<DuiMenuItem>,
    modifier: Modifier = Modifier,
    anchor: @Composable () -> Unit,
) {
    val spacing = DuiTheme.spacing
    Box(modifier) {
        anchor()
        if (expanded) {
            Popup(
                popupPositionProvider = BelowAnchorEnd,
                onDismissRequest = onDismissRequest,
                properties = PopupProperties(focusable = true),
            ) {
                Column(
                    Modifier
                        .width(IntrinsicSize.Max)
                        .widthIn(min = spacing.space7 * 3)
                        .background(DuiTheme.colors.surfaceHigh, DuiTheme.shapes.md)
                        .padding(vertical = spacing.space2),
                ) {
                    items.forEach { item ->
                        val source = remember { MutableInteractionSource() }
                        DuiText(
                            item.label,
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = spacing.space7)
                                .duiFocusRing(source, DuiTheme.shapes.sm)
                                .duiClickable(source, Role.Button, onClick = { onDismissRequest(); item.onClick() })
                                .padding(horizontal = spacing.space4, vertical = spacing.space3),
                            style = DuiTextStyle.Label,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/** Below the anchor, end-aligned with it; flips above when there is no room underneath. */
private val BelowAnchorEnd = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = (anchorBounds.right - popupContentSize.width).coerceAtLeast(0)
        val below = anchorBounds.bottom
        val y = if (below + popupContentSize.height <= windowSize.height) below else (anchorBounds.top - popupContentSize.height).coerceAtLeast(0)
        return IntOffset(x, y)
    }
}
