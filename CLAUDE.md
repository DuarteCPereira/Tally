## DUI-lib design system

Before writing or changing any UI code, read `docs/dui-design-system/README.md` and `docs/dui-design-system/tokens.json`. For each component, read `docs/dui-design-system/components/<Name>/README.md`.

- The `preview.html` files and `components/bundle.css` are web mockups of the approved look. Use them as a visual and spacing reference only. The real implementation is Kotlin in Compose Multiplatform.
- Build the library in `commonMain` on `compose.foundation`, not Material 3.
- Every token has a light and a dark value. Expose them through a `DuiTheme { }` wrapper and `CompositionLocal`s (`DuiColors`, `DuiTypography`, `DuiSpacing`, `DuiShapes`).
- Do not hardcode colors, sizes or radii in components. Use the tokens.
- Keep platform-specific code behind `expect/actual`.
- Work in small steps and show the theme before building components.
- Build simple components on `compose.foundation`. For complex ones (text fields, dialogs, pickers, sheets) wrapping Material 3 internally is fine, fully restyled from the tokens and never exposed in the public API.
