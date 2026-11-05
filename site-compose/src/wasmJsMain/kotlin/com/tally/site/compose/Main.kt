package com.tally.site.compose

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window

/** Mounts the Compose website into the page's `<body>`, which Compose fills with one canvas. */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = requireNotNull(document.body) { "index.html must have a <body> to mount into" }
    // Read once at start: changing the OS setting mid-visit applies on the next load.
    val reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches
    ComposeViewport(body) {
        TallyWebsite(reduceMotion = reduceMotion)
    }
}
