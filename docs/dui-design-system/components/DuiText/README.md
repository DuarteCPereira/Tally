Text in one of the five DUI styles: `display`, `title`, `label`, `body` or `caption`.

Use it for every piece of text so that size, weight and line height always come from the type tokens. Hierarchy comes from the style and from the colour tokens `text-primary` and `text-secondary`, never from extra weights. Sentence case, and no exclamation marks.

**Planned Compose API**: `DuiText(text, modifier, style = DuiTextStyle.Body, color = DuiTheme.colors.textPrimary, maxLines, textAlign)`. The consumer provides the string and the colour token.
