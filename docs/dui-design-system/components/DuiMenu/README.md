A small menu of text items shown below an anchor.

It uses `surface-high` with `radius-md` so it reads as raised without a shadow. Items use `label` in `text-primary` and are at least `space-7` tall. It opens below the anchor, aligned to its end, and flips above when there is no room. Destructive items are plain text like the others; the confirmation step carries the weight.

**Planned Compose API**: `DuiMenu(expanded, onDismissRequest, items: List<DuiMenuItem(label, onClick)>, modifier, anchor)`.
