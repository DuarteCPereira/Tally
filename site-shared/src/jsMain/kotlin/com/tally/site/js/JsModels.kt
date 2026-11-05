@file:OptIn(ExperimentalJsExport::class)
@file:Suppress("LongParameterList") // Exported DTOs mirror the page, one constructor parameter per field.

package com.tally.site.js

import com.tally.site.SiteContent
import com.tally.site.StatsSummary

/*
 * Plain, exported mirrors of the common model for TypeScript. Each becomes a class with readonly
 * fields in the generated `.d.ts`. Enums are passed as lowercase strings (a TypeScript-friendly
 * union), lists as arrays.
 */

/** A link. `target.kind` is "section" or "url". */
@JsExport
public class JsLink(public val label: String, public val target: Target, public val badge: String?) {
    /** Where the link goes. */
    public class Target(public val kind: String, public val value: String)
}

/** "01 · Build". */
@JsExport
public class JsEyebrow(public val index: String, public val text: String)

/** A download button. */
@JsExport
public class JsStoreLink(public val platform: String, public val target: JsLink.Target, public val primary: Boolean)

/** A label on the hero phone. `side` is "start" or "end". */
@JsExport
public class JsCallout(public val text: String, public val side: String, public val topFraction: Double)

/** The hero. `screen` is an app screen id: "run", "rest", "editor" or "summary". */
@JsExport
public class JsHero(
    public val chip: String,
    public val title: String,
    public val body: String,
    public val stores: Array<JsStoreLink>,
    public val tagline: String,
    public val screen: String,
    public val callouts: Array<JsCallout>,
)

/** A row of the principle card. */
@JsExport
public class JsFact(public val term: String, public val value: String, public val accent: Boolean)

/** The principle section. */
@JsExport
public class JsPrinciple(
    public val eyebrow: JsEyebrow,
    public val title: String,
    public val body: String,
    public val facts: Array<JsFact>,
)

/** A "how it works" step. */
@JsExport
public class JsStep(public val eyebrow: JsEyebrow, public val title: String, public val body: String, public val screen: String)

/** A feature card. `icon` is "sets", "search", "timer", "sound", "trend" or "theme". */
@JsExport
public class JsFeature(public val index: String, public val title: String, public val body: String, public val icon: String)

/** A titled group of items: the steps and the features. */
@JsExport
public class JsSection<T>(public val eyebrow: JsEyebrow, public val title: String, public val items: Array<T>)

/** The statistics teaser copy. */
@JsExport
public class JsStatsTeaser(
    public val eyebrow: JsEyebrow,
    public val badge: String,
    public val title: String,
    public val body: String,
    public val previewNote: String,
)

/** The closing call to action. */
@JsExport
public class JsDownload(public val title: String, public val body: String, public val stores: Array<JsStoreLink>)

/** The footer. */
@JsExport
public class JsFooter(public val note: String, public val links: Array<JsLink>)

/** The whole page. */
@JsExport
public class JsSiteContent(
    public val brand: String,
    public val nav: Array<JsLink>,
    public val headerAction: JsLink,
    public val hero: JsHero,
    public val principle: JsPrinciple,
    public val steps: JsSection<JsStep>,
    public val features: JsSection<JsFeature>,
    public val stats: JsStatsTeaser,
    public val download: JsDownload,
    public val footer: JsFooter,
)

/** A small labelled number under the chart. */
@JsExport
public class JsStatTile(public val label: String, public val value: String)

/** The statistics card for one range. `trend` is "up" or "down". */
@JsExport
public class JsStatsSummary(
    public val range: String,
    public val title: String,
    public val volume: String,
    public val change: String,
    public val trend: String,
    public val points: Array<Double>,
    public val chartDescription: String,
    public val tiles: Array<JsStatTile>,
)

internal fun SiteContent.toJs() = JsSiteContent(
    brand = brand,
    nav = nav.map { it.link.toJs(badge = it.badge) }.toTypedArray(),
    headerAction = headerAction.toJs(),
    hero = with(hero) {
        JsHero(
            chip = chip,
            title = title,
            body = body,
            stores = stores.map { it.toJs() }.toTypedArray(),
            tagline = tagline,
            screen = screen.name.lowercase(),
            callouts = callouts.map { it.toJs() }.toTypedArray(),
        )
    },
    principle = with(principle) {
        JsPrinciple(
            eyebrow = eyebrow.toJs(),
            title = title,
            body = body,
            facts = facts.map { JsFact(term = it.term, value = it.value, accent = it.accent) }.toTypedArray(),
        )
    },
    steps = JsSection(
        eyebrow = steps.eyebrow.toJs(),
        title = steps.title,
        items = steps.items.map {
            JsStep(eyebrow = it.eyebrow.toJs(), title = it.title, body = it.body, screen = it.screen.name.lowercase())
        }.toTypedArray(),
    ),
    features = JsSection(
        eyebrow = features.eyebrow.toJs(),
        title = features.title,
        items = features.items.map {
            JsFeature(index = it.index, title = it.title, body = it.body, icon = it.icon.name.lowercase())
        }.toTypedArray(),
    ),
    stats = with(stats) {
        JsStatsTeaser(eyebrow = eyebrow.toJs(), badge = badge, title = title, body = body, previewNote = previewNote)
    },
    download = JsDownload(
        title = download.title,
        body = download.body,
        stores = download.stores.map { it.toJs() }.toTypedArray(),
    ),
    footer = JsFooter(note = footer.note, links = footer.links.map { it.toJs() }.toTypedArray()),
)

internal fun StatsSummary.toJs() = JsStatsSummary(
    range = range.label,
    title = title,
    volume = volume,
    change = change,
    trend = trend.name.lowercase(),
    points = points.map { it.toDouble() }.toTypedArray(),
    chartDescription = chartDescription,
    tiles = tiles.map { JsStatTile(label = it.label, value = it.value) }.toTypedArray(),
)
