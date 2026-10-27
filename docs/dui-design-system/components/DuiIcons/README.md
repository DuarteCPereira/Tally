A set of outline icons on a 24px grid with a 1.5px round stroke, plus `DuiIcon` to draw them.

Icons take their colour from the `tint` passed to `DuiIcon`: `text-primary` by default, `accent` when active, `text-secondary` when inactive and `text-disabled` when disabled. An icon never replaces a label in navigation. Give an icon a content description only when no text sits next to it.

Included: Home, History, Settings, Sound, Vibration, Back, Plus, Minus, Check, Close, More, ArrowUp, ArrowDown, TrendUp, Sets and Spark.

**Planned Compose API**: `DuiIcon(icon: ImageVector, contentDescription, modifier, tint, size)` and `DuiIcons.<Name>`.
