A full-width, pill-shaped button; `primary` is the default and there is at most one per screen.

**Variants**
- `primary`: `fill-primary` with `on-primary` label. Inverts between themes. The main action.
- `accent`: `accent` with `on-accent`. Use instead of primary only when the action must stand out, and never alongside a primary.
- `secondary`: `surface` with `text-primary`. Supporting actions. Inside a card or dialog it uses `surface-high` so it stays visible.
- `ghost`: no fill, `accent` label. Inline or low-priority actions.
- `disabled`: `surface` (or `surface-high` inside a card or dialog) with `text-disabled`. Prefer leaving the button enabled and explaining the problem on tap.

**Specs**: height `space-7` (48px), horizontal padding `space-5`, shape `radius-full`, text style `label`. Focus ring: 2px `focus`, 2px offset.

**Copy**: verb first, one to three words, sentence case, no punctuation ("Invest", "Add to watchlist").

**Planned Compose API**: `DuiButton(text, onClick, modifier, style = DuiButtonStyle.Primary, enabled = true)`. The consumer provides the label and the click handler.
