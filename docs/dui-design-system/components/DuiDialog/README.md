A modal dialog: a `surface` card with `radius-lg` on a `scrim`, a `title`, scrollable content and stacked buttons.

Use `DuiButton` for the buttons: `Primary` for the confirm action and `Ghost` for dismissing. There is no shadow. With `dismissible = false`, back and outside taps do nothing (for example a rest timer). Keep titles as questions or short statements in sentence case.

**Planned Compose API**: `DuiDialog(onDismissRequest, title, confirmButton, modifier, dismissButton = null, dismissible = true, content)`.

Controls placed inside a dialog step up one level so they stay visible without borders: text fields, secondary buttons, filled icon buttons and the segmented control use `surface-high` there instead of `surface`. The same applies inside a `Surface` card.
