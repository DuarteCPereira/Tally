package com.tally.site

/**
 * Everything the Tally website says, in one typed model, so the Compose site and the React site
 * render exactly the same page. The sites only decide how it looks.
 *
 * Anchors ([SectionId]) and [AppScreen]s are plain values here; each site maps them to its own
 * scrolling and its own image assets.
 */
public data class SiteContent(
    public val brand: String,
    public val nav: List<NavItem>,
    public val headerAction: Link,
    public val hero: Hero,
    public val principle: Principle,
    public val steps: Steps,
    public val features: Features,
    public val stats: StatsTeaser,
    public val download: Download,
    public val footer: Footer,
)

/** The page sections that can be linked to from the header. [anchor] is the URL fragment. */
public enum class SectionId(public val anchor: String) {
    Top("top"),
    HowItWorks("how"),
    Features("features"),
    Statistics("stats"),
    Download("download"),
}

/** A real app screen shown inside a phone frame. Each site supplies the image for it. */
public enum class AppScreen { Run, Rest, Editor, Summary }

/** Where a link goes: a section on this page, or an external URL. */
public sealed interface LinkTarget {
    /** Scrolls to a section of this page. */
    public data class Section(public val id: SectionId) : LinkTarget

    /** Leaves the page. */
    public data class Url(public val href: String) : LinkTarget
}

/** A labelled [LinkTarget]. */
public data class Link(public val label: String, public val target: LinkTarget)

/** A header link. [badge] marks a feature that is not live yet. */
public data class NavItem(public val link: Link, public val badge: String? = null)

/** "Get it for Android". [primary] is the most common platform, drawn as the primary button. */
public data class StoreLink(
    public val platform: String,
    public val target: LinkTarget,
    public val primary: Boolean,
)

/** A small mono label above a title: "01 · Build". */
public data class Eyebrow(public val index: String, public val text: String)

/**
 * A short mono label pointing at a spot on the hero phone. [side] is where the label sits, and
 * [topFraction] is how far down the phone it points, from 0 (top) to 1 (bottom).
 */
public data class Callout(public val text: String, public val side: Side, public val topFraction: Float) {
    /** Which side of the phone the label sits on. */
    public enum class Side { Start, End }
}

/** The first section: the one headline of the page, the download buttons and a live app screen. */
public data class Hero(
    public val chip: String,
    public val title: String,
    public val body: String,
    public val stores: List<StoreLink>,
    public val tagline: String,
    public val screen: AppScreen,
    public val callouts: List<Callout>,
)

/** A definition row in the principle card: "taps to log a set" → "1". [accent] colours the value. */
public data class Fact(public val term: String, public val value: String, public val accent: Boolean = false)

/** The "one screen, one action" statement, with its [facts]. */
public data class Principle(
    public val eyebrow: Eyebrow,
    public val title: String,
    public val body: String,
    public val facts: List<Fact>,
)

/** One step of "how it works", shown with a real [screen]. */
public data class Step(
    public val eyebrow: Eyebrow,
    public val title: String,
    public val body: String,
    public val screen: AppScreen,
)

/** The "how it works" section. */
public data class Steps(public val eyebrow: Eyebrow, public val title: String, public val items: List<Step>)

/** Icons the feature cards use. Each site maps them to its own icon set. */
public enum class FeatureIcon { Sets, Search, Timer, Sound, Trend, Theme }

/** One feature card. */
public data class Feature(
    public val index: String,
    public val title: String,
    public val body: String,
    public val icon: FeatureIcon,
)

/** The features grid. */
public data class Features(public val eyebrow: Eyebrow, public val title: String, public val items: List<Feature>)

/** The statistics teaser. The numbers come from [StatsPreview], not from here. */
public data class StatsTeaser(
    public val eyebrow: Eyebrow,
    public val badge: String,
    public val title: String,
    public val body: String,
    public val previewNote: String,
)

/** The closing call to action. */
public data class Download(public val title: String, public val body: String, public val stores: List<StoreLink>)

/** The footer [note] ("© 2026 Tally") and its links. */
public data class Footer(public val note: String, public val links: List<Link>)
