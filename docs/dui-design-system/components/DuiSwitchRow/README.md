A setting row with an optional icon, a `label` title, an optional `caption` description and an on/off switch.

The switch is `accent` when on and `surface-high` when off, with a round thumb. The whole row is the touch target and is at least `space-7` tall. The icon is `accent`. Describe what the setting does, not its state.

**Planned Compose API**: `DuiSwitchRow(title, checked, onCheckedChange, modifier, description = null, leading = null)`.
