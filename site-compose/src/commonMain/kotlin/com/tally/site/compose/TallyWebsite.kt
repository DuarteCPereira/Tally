package com.tally.site.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalUriHandler
import com.duarte.dui.components.DuiNavLink
import com.duarte.dui.components.DuiSiteHeader
import com.duarte.dui.theme.DuiTheme
import com.tally.site.LinkTarget
import com.tally.site.SectionId
import com.tally.site.SiteContent
import com.tally.site.TallySite
import kotlinx.coroutines.launch

/** True when the user asked the OS for less motion. Animations check it before they run. */
internal val LocalReduceMotion = staticCompositionLocalOf { false }

/**
 * The whole landing page: a sticky [DuiSiteHeader] over one scrolling column of sections.
 *
 * Compose draws to a single canvas, so there are no DOM anchors. Each section reports its offset
 * inside the scrolling column, and header links animate the scroll to it.
 *
 * @param reduceMotion pass the browser's `prefers-reduced-motion`; it stops the tally mark and the
 * live dot from animating.
 */
@Composable
fun TallyWebsite(
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    content: SiteContent = TallySite.content,
) {
    DuiTheme {
        CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
            Page(content, modifier)
        }
    }
}

@Composable
private fun Page(content: SiteContent, modifier: Modifier = Modifier) {
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val offsets = remember { mutableStateMapOf<SectionId, Int>() }
    val onLink: (LinkTarget) -> Unit = { target ->
        when (target) {
            is LinkTarget.Url -> uriHandler.openUri(target.href)
            is LinkTarget.Section -> scope.launch { scroll.animateScrollTo(offsets[target.id] ?: 0) }
        }
    }
    val anchor: (SectionId) -> Modifier = { id ->
        Modifier.onGloballyPositioned { offsets[id] = it.positionInParent().y.toInt() }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(DuiTheme.colors.bg),
    ) {
        DuiSiteHeader(
            links = content.nav.map { DuiNavLink(it.link.label, it.link.target.key, it.badge) },
            onLinkClick = { link -> content.nav.firstOrNull { it.link.target.key == link.target }?.link?.target?.let(onLink) },
            brand = content.brand,
            onBrandClick = { onLink(LinkTarget.Section(SectionId.Top)) },
            action = { HeaderAction(content.headerAction, onLink) },
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scroll),
        ) {
            HeroSection(content.hero, onLink, anchor(SectionId.Top))
            PrincipleSection(content.principle)
            StepsSection(content.steps, anchor(SectionId.HowItWorks))
            FeaturesSection(content.features, anchor(SectionId.Features))
            StatsSection(content.stats, anchor(SectionId.Statistics))
            DownloadSection(content.download, onLink, anchor(SectionId.Download))
            SiteFooter(content.footer, onLink)
        }
    }
}

/** A stable string for a target, used as the [DuiNavLink.target] key. */
internal val LinkTarget.key: String
    get() = when (this) {
        is LinkTarget.Section -> "#${id.anchor}"
        is LinkTarget.Url -> href
    }
