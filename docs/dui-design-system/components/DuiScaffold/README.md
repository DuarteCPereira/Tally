The screen container: `bg` background, the safe-area insets, an optional top bar and an optional bottom bar.

Insets are handled at the top and sides always. The bottom inset is handled by the scaffold only when there is no bottom bar, because `DuiBottomBar` pads itself. Content gets the full remaining height.

**Planned Compose API**: `DuiScaffold(modifier, topBar = {}, bottomBar = null, content: (PaddingValues) -> Unit)`.
