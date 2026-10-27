A tile with a `caption` label, an optional icon and a value.

The tile is `surface` with `radius-lg` and `space-4` padding. The value uses `title`, or `display` with `emphasis = true`; use `emphasis` on one tile per screen. Labels are sentence case ("Total sets"), not capitals. The app supplies the value already formatted.

**Planned Compose API**: `DuiStatTile(label, value, modifier, icon = null, emphasis = false)`.
