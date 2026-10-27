A bottom tab bar with four destinations, each an outline icon over a `caption` label.

The active tab uses `accent`; the others use `text-secondary`. A 1px `divider` hairline sits on top, and the bar is `bg` with no shadow. Icons are 24px with a 1.5px round stroke. Keep it to five tabs at most and always show the label.

**Planned Compose API**: `DuiBottomBar(items: List<DuiTab>, selectedIndex, onSelect)`. The consumer provides the destinations; the library handles the safe-area inset on iOS and Android.
