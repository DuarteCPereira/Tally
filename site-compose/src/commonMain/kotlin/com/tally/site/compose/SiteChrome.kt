package com.tally.site.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import com.duarte.dui.components.DuiButton
import com.duarte.dui.components.DuiButtonSize
import com.duarte.dui.components.DuiButtonStyle
import com.duarte.dui.components.DuiDeviceFrame
import com.duarte.dui.components.DuiIcons
import com.duarte.dui.components.DuiNavLink
import com.duarte.dui.components.DuiSiteFooter
import com.duarte.dui.components.DuiStoreButton
import com.duarte.dui.theme.DuiTheme
import com.tally.site.AppScreen
import com.tally.site.FeatureIcon
import com.tally.site.Footer
import com.tally.site.Link
import com.tally.site.LinkTarget
import com.tally.site.StoreLink
import com.tally.site.compose.resources.Res
import com.tally.site.compose.resources.screen_editor
import com.tally.site.compose.resources.screen_rest
import com.tally.site.compose.resources.screen_run
import com.tally.site.compose.resources.screen_summary
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** The small "Download" button in the header. */
@Composable
internal fun HeaderAction(link: Link, onLink: (LinkTarget) -> Unit) {
    DuiButton(text = link.label, onClick = { onLink(link.target) }, size = DuiButtonSize.Small)
}

/** The download buttons, side by side, wrapping on narrow screens. */
@Composable
internal fun StoreButtons(
    stores: List<StoreLink>,
    onLink: (LinkTarget) -> Unit,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(DuiTheme.spacing.space2),
) {
    FlowRow(
        modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = Arrangement.spacedBy(DuiTheme.spacing.space2),
    ) {
        stores.forEach { store ->
            DuiStoreButton(
                platform = store.platform,
                onClick = { onLink(store.target) },
                style = if (store.primary) DuiButtonStyle.Primary else DuiButtonStyle.Secondary,
            )
        }
    }
}

/** A real app screenshot in a phone frame, laid out at phone size and scaled as a whole. */
@Composable
internal fun AppScreenFrame(screen: AppScreen, scale: Float, modifier: Modifier = Modifier) {
    DuiDeviceFrame(modifier, scale = scale) {
        Image(
            painter = painterResource(screen.image),
            contentDescription = screen.description,
            modifier = Modifier.fillMaxSize(),
            // Screenshots are slightly taller than the frame; keep the top and lose the gesture bar.
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
        )
    }
}

@Composable
internal fun SiteFooter(footer: Footer, onLink: (LinkTarget) -> Unit) {
    DuiSiteFooter(
        links = footer.links.map { DuiNavLink(it.label, it.target.key) },
        note = footer.note,
        onLinkClick = { link -> footer.links.firstOrNull { it.target.key == link.target }?.target?.let(onLink) },
    )
}

private val AppScreen.image: DrawableResource
    get() = when (this) {
        AppScreen.Run -> Res.drawable.screen_run
        AppScreen.Rest -> Res.drawable.screen_rest
        AppScreen.Editor -> Res.drawable.screen_editor
        AppScreen.Summary -> Res.drawable.screen_summary
    }

private val AppScreen.description: String
    get() = when (this) {
        AppScreen.Run -> "Tally run screen: round 1 of a superset, with the rep counter and the Complete set button"
        AppScreen.Rest -> "Tally rest timer counting down between sets"
        AppScreen.Editor -> "Tally workout editor with exercises and a superset"
        AppScreen.Summary -> "Tally session summary after a finished workout"
    }

internal val FeatureIcon.vector: ImageVector
    get() = when (this) {
        FeatureIcon.Sets -> DuiIcons.Sets
        FeatureIcon.Search -> DuiIcons.Search
        FeatureIcon.Timer -> DuiIcons.History
        FeatureIcon.Sound -> DuiIcons.Speaker
        FeatureIcon.Trend -> DuiIcons.TrendUp
        FeatureIcon.Theme -> DuiIcons.Contrast
    }
