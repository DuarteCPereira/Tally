## DUI-lib design system

The library lives in `:dui-lib` (package `com.duarte.dui`). The repo keeps only what the code must match: `docs/dui-design-system/tokens.json` (the tokens) and `docs/dui-design-system/README.md` (the principles).

Before writing or changing any UI code, read both of those files. For component specs and previews, read the DUI-lib artifact: https://claude.ai/artifact/AennrarNF2r6sPFfMP77cP (private; read it with the Artifact tool, `action: "read"`, path `project/components/<Name>/README.md`, previews at `project/components/<Name>/preview.html`).

- The artifact's `preview.html` files and `bundle.css` are web mockups of the approved look. Use them as a visual and spacing reference only. The real implementation is Kotlin in Compose Multiplatform.
- The repo is the source of truth for tokens. When a token changes, change `tokens.json` and the Kotlin theme together, then republish `tokens.json` to the artifact.
- When you add or change a component, update its README and preview in the artifact in the same piece of work. Do not add component docs to the repo.
- Build the library in `commonMain` on `compose.foundation`, not Material 3.
- Every token has a light and a dark value. Expose them through a `DuiTheme { }` wrapper and `CompositionLocal`s (`DuiColors`, `DuiTypography`, `DuiSpacing`, `DuiShapes`).
- Do not hardcode colors, sizes or radii in components. Use the tokens.
- Keep platform-specific code behind `expect/actual`.
- Work in small steps and show the theme before building components.
- Build simple components on `compose.foundation`. For complex ones (text fields, dialogs, pickers, sheets) wrapping Material 3 internally is fine, fully restyled from the tokens and never exposed in the public API.
