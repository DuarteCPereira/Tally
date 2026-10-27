package com.duarte.dui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.duarte.dui.internal.DuiRawText
import com.duarte.dui.internal.duiClickable
import com.duarte.dui.internal.duiFocusRing
import com.duarte.dui.theme.DuiTheme

/**
 * A `title` with an optional `accent` text action on the right. Includes `space-5` above and
 * `space-2` below, so no extra spacing is needed around it.
 */
@Composable
fun DuiSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: () -> Unit = {},
) {
    val spacing = DuiTheme.spacing
    Row(
        modifier.fillMaxWidth().padding(top = spacing.space5, bottom = spacing.space2),
        horizontalArrangement = Arrangement.spacedBy(spacing.space4),
    ) {
        DuiRawText(
            title,
            DuiTheme.typography.title,
            DuiTheme.colors.textPrimary,
            Modifier.weight(1f).alignByBaseline().semantics { heading() },
        )
        if (actionText != null) {
            val source = remember { MutableInteractionSource() }
            DuiRawText(
                actionText,
                DuiTheme.typography.label,
                DuiTheme.colors.accent,
                Modifier
                    .alignByBaseline()
                    .duiFocusRing(source, DuiTheme.shapes.sm)
                    .duiClickable(source, Role.Button, onClick = onAction),
                maxLines = 1,
            )
        }
    }
}
