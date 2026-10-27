A single row with a leading avatar, a title and subtitle, and a right-aligned value with a change figure.

Rows sit on `surface` or `bg`. Separate them with a 1px `divider` hairline between rows only, never a border around each row, and never a shadow. Vertical padding is `space-3`. Title and value use `label`, subtitle and change use `caption`. The change figure uses `positive` or `negative` and always carries a sign. The selected row gets `accent-soft`.

Set `divider` on every row except the first to draw the hairline between rows. The leading slot takes `DuiAvatar` with initials or with an icon.

**Planned Compose API**: `DuiListRow(title, modifier, subtitle, value, change, trend = DuiTrend.Up, selected = false, divider = false, onClick = null, leading = null)` and `DuiAvatar(initials)` or `DuiAvatar(icon)`. The consumer provides formatted strings, so the library never formats currency or percentages.
