@file:OptIn(ExperimentalJsExport::class)

package com.tally.site.js

import com.tally.site.Callout
import com.tally.site.Eyebrow
import com.tally.site.Link
import com.tally.site.LinkTarget
import com.tally.site.SampleSessions
import com.tally.site.StatsPreview
import com.tally.site.StatsRange
import com.tally.site.StoreLink
import com.tally.site.TallySite

/*
 * The npm entry points of `tally-site-shared`, used by the React site.
 *
 * Kotlin's `List`, sealed types and enums do not map cleanly to TypeScript, so this layer turns
 * the common model into plain exported classes with arrays and string unions. Nothing here holds
 * logic; it only reshapes [TallySite] and [StatsPreview] at the JS boundary. They are top-level
 * functions because an exported `object` surfaces in TypeScript as `X.getInstance()`.
 */

private val siteContent: JsSiteContent by lazy { TallySite.content.toJs() }

/** The page copy. Built once and shared between calls. */
@JsExport
public fun getSiteContent(): JsSiteContent = siteContent

/** The chip labels under the chart, in order ("1W", "1M", ...). */
@JsExport
public fun getStatsRanges(): Array<String> = StatsRange.entries.map { it.label }.toTypedArray()

/** The range the chart opens on. */
@JsExport
public fun getDefaultStatsRange(): String = StatsRange.Month.label

/**
 * The statistics card for the chip [label] over the sample sessions.
 *
 * @throws IllegalArgumentException when [label] is not one of [getStatsRanges].
 */
@JsExport
public fun getStats(label: String): JsStatsSummary {
    val range = requireNotNull(StatsRange.entries.firstOrNull { it.label == label }) {
        "Unknown range '$label', expected one of ${getStatsRanges().joinToString()}"
    }
    return StatsPreview.summarize(SampleSessions.all, range).toJs()
}

//region Mapping

/** `section` targets carry the anchor without the '#'; `url` targets carry the full href. */
internal fun LinkTarget.toJs(): JsLink.Target = when (this) {
    is LinkTarget.Section -> JsLink.Target(kind = "section", value = id.anchor)
    is LinkTarget.Url -> JsLink.Target(kind = "url", value = href)
}

internal fun Link.toJs(badge: String? = null) = JsLink(label = label, target = target.toJs(), badge = badge)

internal fun Eyebrow.toJs() = JsEyebrow(index = index, text = text)

internal fun StoreLink.toJs() = JsStoreLink(platform = platform, target = target.toJs(), primary = primary)

internal fun Callout.toJs() = JsCallout(
    text = text,
    side = side.name.lowercase(),
    topFraction = topFraction.toDouble(),
)

//endregion
