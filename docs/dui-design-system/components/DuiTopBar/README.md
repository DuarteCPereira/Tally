A top bar with an optional back action, an optional text action and an optional trailing caption.

The back action is an icon with a visible label ("Back", "Exit"), because icons never appear without a label in navigation. The text action uses `label` in `accent` and starts with a verb ("Save"). The trailing caption uses `caption` in `text-secondary`. There is no title and no divider; the screen's own title sits below it in the `title` style.

**Planned Compose API**: `DuiTopBar(modifier, onBack = null, backLabel = "Back", actionText = null, onAction = {}, trailing = null)`.
