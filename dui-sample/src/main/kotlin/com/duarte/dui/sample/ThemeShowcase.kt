package com.duarte.dui.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.duarte.dui.theme.DuiTheme

/** Phase 1 check: every token rendered with the current [DuiTheme]. */
@Composable
fun ThemeShowcase(themeName: String) {
    val c = DuiTheme.colors
    val t = DuiTheme.typography
    val s = DuiTheme.spacing
    Column(Modifier.fillMaxWidth().padding(s.space4)) {
        BasicText("$themeName theme", style = t.title.copy(color = c.textPrimary))
        Spacer(Modifier.height(s.space5))

        Caption("Colour")
        Column(Modifier.background(c.surface, DuiTheme.shapes.lg).padding(horizontal = s.space4, vertical = s.space2)) {
            listOf(
                "bg" to c.bg, "surface" to c.surface, "surface-high" to c.surfaceHigh, "divider" to c.divider,
                "text-primary" to c.textPrimary, "text-secondary" to c.textSecondary, "text-disabled" to c.textDisabled,
                "fill-primary" to c.fillPrimary, "on-primary" to c.onPrimary, "accent" to c.accent, "on-accent" to c.onAccent,
                "accent-soft" to c.accentSoft, "positive" to c.positive, "negative" to c.negative, "focus" to c.focus,
            ).forEach { (name, color) -> Swatch(name, color) }
        }
        Spacer(Modifier.height(s.space5))

        Caption("Type")
        listOf(
            Triple("display", t.display, "€12,480.32"),
            Triple("title", t.title, "Your portfolio"),
            Triple("label", t.label, "Invest"),
            Triple("body", t.body, "Apple, 3 shares"),
            Triple("caption", t.caption, "Savings plan"),
        ).forEach { (name, style, sample) -> TypeSample(name, style, sample) }
        Spacer(Modifier.height(s.space5))

        Caption("Spacing")
        listOf(
            "space-1" to s.space1, "space-2" to s.space2, "space-3" to s.space3, "space-4" to s.space4,
            "space-5" to s.space5, "space-6" to s.space6, "space-7" to s.space7,
        ).forEach { (name, size) -> SpacingBar(name, size) }
        Spacer(Modifier.height(s.space5))

        Caption("Radius")
        Row(horizontalArrangement = Arrangement.spacedBy(s.space4)) {
            val sh = DuiTheme.shapes
            listOf("sm" to sh.sm, "md" to sh.md, "lg" to sh.lg, "full" to sh.full).forEach { (name, shape) -> RadiusSample(name, shape) }
        }
        Spacer(Modifier.height(s.space6))
    }
}

@Composable
private fun Caption(text: String) {
    BasicText(text, Modifier.padding(bottom = DuiTheme.spacing.space2), style = DuiTheme.typography.caption.copy(color = DuiTheme.colors.textSecondary))
}

@Composable
private fun Swatch(name: String, color: Color) {
    val s = DuiTheme.spacing
    Row(Modifier.fillMaxWidth().padding(vertical = s.space1), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(s.space5).background(color, DuiTheme.shapes.sm))
        Spacer(Modifier.width(s.space3))
        BasicText(name, Modifier.weight(1f), style = DuiTheme.typography.label.copy(color = DuiTheme.colors.textPrimary))
        BasicText(color.hex(), style = DuiTheme.typography.caption.copy(color = DuiTheme.colors.textSecondary))
    }
}

@Composable
private fun TypeSample(name: String, style: TextStyle, sample: String) {
    Column(Modifier.padding(bottom = DuiTheme.spacing.space3)) {
        BasicText(sample, style = style.copy(color = DuiTheme.colors.textPrimary))
        BasicText(
            "$name · ${style.fontSize.value.toInt()}/${style.lineHeight.value.toInt()} · ${style.fontWeight?.weight}",
            style = DuiTheme.typography.caption.copy(color = DuiTheme.colors.textSecondary),
        )
    }
}

@Composable
private fun SpacingBar(name: String, size: Dp) {
    val s = DuiTheme.spacing
    Row(Modifier.padding(vertical = s.space1), verticalAlignment = Alignment.CenterVertically) {
        BasicText(name, Modifier.width(s.space7 + s.space6), style = DuiTheme.typography.caption.copy(color = DuiTheme.colors.textSecondary))
        Box(Modifier.width(size).height(s.space2).background(DuiTheme.colors.accent, DuiTheme.shapes.full))
        Spacer(Modifier.width(s.space2))
        BasicText("${size.value.toInt()}", style = DuiTheme.typography.caption.copy(color = DuiTheme.colors.textSecondary))
    }
}

@Composable
private fun RadiusSample(name: String, shape: Shape) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(DuiTheme.spacing.space7 + DuiTheme.spacing.space4).background(DuiTheme.colors.surfaceHigh, shape))
        Spacer(Modifier.height(DuiTheme.spacing.space1))
        BasicText(name, style = DuiTheme.typography.caption.copy(color = DuiTheme.colors.textSecondary))
    }
}

private fun Color.hex(): String = "#" + (toArgb() and 0xFFFFFF).toString(16).padStart(6, '0')
