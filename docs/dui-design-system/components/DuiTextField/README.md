A text input on `surface` with `radius-md`, a `caption` label above and an optional error message below.

The text uses `body` in `text-primary`; the placeholder uses `text-secondary`. There is no border. Focus shows the 2px `focus` ring with a 2px offset. The error message uses `caption` in `negative` and always says what to do ("Enter an exercise name."). Supports single-line, number (through keyboard options) and multi-line input.

**Planned Compose API**: `DuiTextField(value, onValueChange, modifier, label = null, placeholder = null, error = null, singleLine = true, minLines = 1, keyboardOptions)`. The app supplies the label and the error text.

Inside a `Surface` card or a dialog the field fills with `surface-high` instead of `surface`, so it stays visible on the layer behind it.
