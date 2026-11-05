# The Tally website, built twice

The same landing page (the "Website" artboard of the Tally screens canvas) exists in two modules, so the two web
approaches can be compared side by side:

| | `:site-compose` | `site-react` |
|---|---|---|
| Stack | Compose Multiplatform, Kotlin/Wasm | Vite + React 19 + TypeScript |
| UI library | `:dui-lib` (the app's own components, now with a `wasmJs` target) | DUI components re-done as React + CSS (`src/components`, `src/dui.css`) |
| Tokens | `DuiTheme` (Kotlin, mirrors `tokens.json` by hand) | `tokens.json` → CSS variables at build time (`tokens.ts`, a Vite plugin) |
| Shared Kotlin | `:site-shared` as a normal Gradle dependency (wasmJs) | `:site-shared` as the npm package `tally-site-shared` (Kotlin/JS + `.d.ts`) |
| Rendering | Skia draws everything into one `<canvas>` | Plain DOM and CSS |

## Module layout

```
site-shared/   KMP library (wasmJs + js). The page copy (TallySite), the stats logic (StatsPreview),
               the sample data and their tests. jsMain adds the @JsExport layer for npm.
site-compose/  KMP app (wasmJs). commonMain holds the page; wasmJsMain mounts it with ComposeViewport.
site-react/    Vite app. Depends on "tally-site-shared": "file:../site-shared/build/dist/js/productionLibrary".
dui-lib/       Gains the web tokens and components: DuiSiteHeader, DuiSiteSection, DuiSiteFooter,
               DuiEyebrow, DuiFeatureCard, DuiDeviceFrame, DuiStoreButton, DuiTallyMark.
```

Both sites read the same `SiteContent` and call the same `StatsPreview.summarize`. The only things each site owns
are layout, styling and the mapping from `AppScreen`/`FeatureIcon` to its own images and icons. Change a sentence in
`TallySite.kt` and both sites change.

## Running them

```sh
# Compose: dev server with hot reload of the wasm build
./gradlew :site-compose:wasmJsBrowserDevelopmentRun
# Compose: static build in site-compose/build/dist/wasmJs/productionExecutable
./gradlew :site-compose:wasmJsBrowserDistribution

# React (needs Node 22.12+ or 24+; `npm run dev` and `npm run build` rebuild the Kotlin package first)
./gradlew :site-shared:jsBrowserProductionLibraryDistribution   # once, before the first npm install
cd site-react && npm install && npm run dev
```

## What I measured

Production builds, served locally without compression, Chrome, 1440 x 900.

| | Compose (Wasm) | React |
|---|---|---|
| Transferred on first load | ~12.5 MB | ~0.6 MB |
| Main code, gzipped | app wasm 776 KB + Skiko wasm 3.2 MB + JS glue 57 KB | JS 89 KB + CSS 10 KB |
| Fonts | 3 TTF files, 1.1 MB, always loaded | woff2 subsets, only the ones used (~70 KB) |
| Text in the DOM (`innerText`) | none: it is a canvas | all of it |
| Accessibility tree | buttons and text runs, no links, headings or landmarks | `banner`, `navigation`, `main`, `link`s with `href`, `h1`–`h3`, `figure`, `tablist` |
| Page code (excluding shared) | ~800 lines of Kotlin, plus ~550 in new dui-lib components | ~500 lines of TSX/TS plus ~170 of CSS |

Visually the two are close to identical in light, dark, desktop and phone widths.

## What the comparison teaches

**Where Compose for web shines**

- *One UI codebase.* The site uses the same `DuiTheme`, `DuiButton`, `DuiStatTile` and `DuiLineChart` as the Android
  and iOS app. A token fix lands everywhere at once. In React every component had to be written again.
- *Real app screens are within reach.* `DuiDeviceFrame` lays its content out at 390 x 844 under a scaled `Density`, so
  it can host a real app composable, not only a screenshot. Today it shows screenshots, because `:shared` (SQLDelight,
  Koin, Navigation 3) has no web target yet; giving it one would let the hero run a live, clickable run screen.
- *Kotlin all the way.* The stats card calls `StatsPreview` directly, with no export layer and no type mapping.

**Where the normal web stack shines**

- *Size and speed.* The Skia engine alone is 8 MB (3.2 MB gzipped) before any page code. For a marketing page that
  is the dominant cost. React's whole page is smaller than Compose's JS glue file plus one font.
- *The web for free.* Anchors are `<a href="#features">` and the browser scrolls; `position: sticky` keeps the header;
  `:focus-visible` handles focus rings; `prefers-color-scheme` and `prefers-reduced-motion` are media queries; text
  is selectable and findable with Ctrl+F; crawlers read it. In Compose each of these needed code (see below), and
  some (SEO, text selection, browser find) are not available at all.
- *Layout.* CSS grid's `repeat(auto-fit, minmax(...))` is one line; Compose needed `ResponsiveGrid` (BoxWithConstraints,
  chunking and intrinsic heights).

**Things Compose needed that the browser gives React for free**

| Need | Compose | React |
|---|---|---|
| Scroll to a section | record each section's offset with `onGloballyPositioned`, `animateScrollTo` | `<a href="#id">` + `scroll-behavior: smooth` |
| Sticky header | put the header above the scrolling `Column` | `position: sticky` |
| Focus ring only for the keyboard | check `LocalInputModeManager` in `duiFocusRing` | `:focus-visible` |
| Reduced motion | read `matchMedia` in `main()`, pass it down a `CompositionLocal` | `@media (prefers-reduced-motion)` |
| Responsive breakpoints | `BoxWithConstraints` in each component | `@media (max-width: 720px)` |
| Hover colour, pointer cursor | `collectIsHoveredAsState`, `pointerHoverIcon` | `:hover`, default for links |

**The Kotlin to npm boundary**

`:site-shared` compiles to an ES module with TypeScript types. Two things did not cross cleanly:

- `List`, enums and sealed interfaces do not map well to TypeScript, so `jsMain` has a small `@JsExport` layer
  (`JsModels.kt`) that turns them into classes with arrays and lowercase string unions.
- An exported `object` shows up as `X.getInstance()` in TypeScript, so the entry points are top-level functions
  (`getSiteContent()`, `getStats(range)`).

The package costs about 70 KB of Kotlin output and stdlib before gzip. It is worth it for logic, such as the
statistics that will one day be shared with the app; for copy alone a JSON file would be simpler.

## Verdict for this project

For the marketing page, the React site is the one to ship: it loads in a fraction of the bytes, is accessible and
indexable, and the shared Kotlin still guarantees that both sites say the same thing. Compose for web becomes the
better fit for the planned **web statistics** page, which is app-like, sits behind a click rather than a search
result, and could reuse the app's real screens and ViewModels once `:shared` gets a `wasmJs` target.
