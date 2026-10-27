# DUI-lib

A small, calm design system for a Compose Multiplatform UI library. It is inspired by the clarity of modern investing apps: near-monochrome, flat, with one big number per screen.

## Principles

- **One number leads.** Each screen has a single `display` value. Everything else is smaller and quieter.
- **Monochrome, one accent.** Black, white and grays do the work. `accent` is the only brand colour; `positive` and `negative` appear only for gains and losses and always with a sign.
- **Layers, not borders.** Cards are a `surface` step off `bg`. Rows are separated by a `divider` hairline. There are no shadows. Controls inside a card or dialog step up to `surface-high` so they stay visible.
- **Both themes from the start.** Every token has a light and a dark value, and every component is previewed in both.
- **One primary action per screen.** `fill-primary` inverts between themes, so it needs no brand colour.

## Visual foundations

| Area | Rule |
|---|---|
| Colour | 16 tokens, light and dark. Text on its backgrounds meets 4.5:1 in both themes. |
| Type | One family (Inter), five styles: `display`, `title`, `label`, `body`, `caption`. Hierarchy comes from size and weight (400 and 500), never from extra colours. |
| Spacing | 4px base: `space-1` to `space-7` (4, 8, 12, 16, 24, 32, 48). |
| Shape | `radius-sm` 8, `radius-md` 12, `radius-lg` 16, `radius-full` for buttons and avatars. |
| Elevation | None. Depth is shown with `surface` and `surface-high`. |

## Content fundamentals

Sentence case everywhere. Buttons start with a verb ("Invest", "Add to watchlist"). Amounts and percentages arrive pre-formatted from the app, always with an explicit sign for changes. No exclamation marks.

## Iconography

Outline icons on a 24px grid with a 1.5px round stroke, drawn in `currentColor` so they follow the text token around them (`accent` when active). Icons never appear without a label in navigation.

## Using this in Compose Multiplatform

Each token maps to a Kotlin property in `commonMain`: colours become a light and dark `DuiColors` class exposed through a `CompositionLocal`, type styles become `DuiTypography`, spacing and radius become `DuiSpacing` and `DuiShapes`. Components are built on `compose.foundation`, not Material 3. Beyond the first six, the library also covers screen structure (`DuiScaffold`, `DuiTopBar`, `DuiCard`), inputs (`DuiTextField`, `DuiSegmentedControl`, `DuiSwitchRow`, `DuiStepper`), overlays (`DuiDialog`, `DuiMenu`) and feedback (`DuiProgressBar`, `DuiLoadingIndicator`, `DuiBadge`), with `DuiText` and `DuiIcons` as the shared primitives. The font is bundled as a Compose resource so it renders the same on Android and iOS.

## Assumptions to confirm

- The accent colour (a calm blue) is a placeholder choice and can be changed in `tokens.json`.
- Inter is a stand-in typeface; any single sans-serif works as long as it keeps weights 400 and 500.
