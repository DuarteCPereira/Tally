A control for adjusting a whole number: a minus button, the value in `display`, and a plus button.

The buttons are round, `surface` and `space-7` wide, which is the minimum touch target. The value is the headline number of its screen, so use it once per screen. Give the buttons content descriptions ("Decrease reps"). A button can be disabled at a limit, for example when the value is 0.

**Planned Compose API**: `DuiStepper(value: String, onDecrement, onIncrement, modifier, decrementEnabled = true, incrementEnabled = true, decrementLabel, incrementLabel)`. The value arrives as a string.
