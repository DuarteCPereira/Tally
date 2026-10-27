A flat container: `surface` (or `accent-soft`), `radius-lg`, `space-4` padding, no border and no shadow.

Use `Surface` tone for grouped content and `Accent` tone for a quiet highlight such as a superset block. Pair `Accent` tone with `accent` text. Pass `onClick` to make the whole card tappable; pressed and focus states follow the tokens.

**Planned Compose API**: `DuiCard(modifier, onClick = null, tone = DuiCardTone.Surface, content)`.

A `Surface` card puts its content "on surface": text fields, secondary buttons, filled icon buttons and the segmented control inside it use `surface-high`. The `Accent` tone does not.
