package com.duarte.dui.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.duarte.dui.theme.DuiTheme

/** The five DUI text styles. Hierarchy comes from size and weight only. */
enum class DuiTextStyle { Display, Title, Label, Body, Caption }

/** Text in one of the five DUI styles. Pass a colour token from [DuiTheme.colors]. */
@Composable
fun DuiText(
    text: String,
    modifier: Modifier = Modifier,
    style: DuiTextStyle = DuiTextStyle.Body,
    color: Color = DuiTheme.colors.textPrimary,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign? = null,
) {
    val t = DuiTheme.typography
    val base = when (style) {
        DuiTextStyle.Display -> t.display
        DuiTextStyle.Title -> t.title
        DuiTextStyle.Label -> t.label
        DuiTextStyle.Body -> t.body
        DuiTextStyle.Caption -> t.caption
    }
    BasicText(
        text,
        modifier,
        base.copy(color = color, textAlign = textAlign ?: TextAlign.Unspecified),
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
    )
}
