A calm block for "nothing here yet" and failure states: an optional icon, a `title`, a `body` and an optional action.

It sits on `surface` with `radius-lg` and `space-5` padding. The icon is `accent`. The action is a `DuiButton`; `Primary` by default. Write the title as a statement and the body as one sentence, in sentence case.

**Planned Compose API**: `DuiEmptyState(title, body, modifier, icon = null, actionText = null, onAction = {}, actionStyle = DuiButtonStyle.Primary)`.
