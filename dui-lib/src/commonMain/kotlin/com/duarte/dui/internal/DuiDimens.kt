package com.duarte.dui.internal

import androidx.compose.ui.unit.dp

/**
 * Fixed component measurements that are specified in the component READMEs and mockups but are
 * not tokens in `tokens.json`. Everything else comes from the theme.
 */
internal object DuiDimens {
    /** README (Principles), ListRow, BottomBar: the 1px divider hairline. */
    val Hairline = 1.dp

    /** PrimaryButton README: focus ring 2px wide with a 2px offset. */
    val FocusRingWidth = 2.dp
    val FocusRingOffset = 2.dp

    /** README (Iconography), BottomBar: 24px icons. The 1.5px stroke is part of the icon itself. */
    val IconSize = 24.dp

    /** ListRow mockup (`.dui-avatar`): 40px avatar. */
    val AvatarSize = 40.dp

    /** LineChart README and mockup: 1.5px line, 96px tall chart. */
    val ChartStroke = 1.5.dp
    val ChartHeight = 96.dp

    /** Icon stroke, also used by the loading indicator arc. */
    val IconStroke = 1.5.dp

    /** ProgressBar: 4dp bar (the app's current run-screen bar height). */
    val ProgressHeight = 4.dp

    /** Switch: 52 x 32 track with a 24dp thumb, so the row still meets the space-7 target. */
    val SwitchTrackWidth = 52.dp
    val SwitchTrackHeight = 32.dp
    val SwitchThumb = 24.dp

    /** BottomBar README: keep it to five tabs at most. */
    const val MaxTabs = 5

    /** DuiSiteHeader README: the small 40px primary button. */
    val SmallButtonHeight = 40.dp

    /** DuiStoreButton README: 56px tall. */
    val StoreButtonHeight = 56.dp

    /** DuiSiteSection README: the 1200px content column and the 48px hairline grid. */
    val SiteContentMaxWidth = 1200.dp
    val SiteGridCell = 48.dp

    /** Website mockup: links hide below 720px. */
    val SiteCompactBreakpoint = 720.dp

    /** DuiDeviceFrame README: 10px bezel, 38px screen corners, app screens laid out at 390 x 844. */
    val DeviceBezel = 10.dp
    val DeviceScreenRadius = 38.dp
    val DeviceScreenWidth = 390.dp
    val DeviceScreenHeight = 844.dp

    /** DuiFeatureCard mockup: 48px round icon chip. */
    val FeatureIconChip = 48.dp
}
