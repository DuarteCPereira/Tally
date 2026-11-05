package com.tally.site

/**
 * The Tally website copy, from the "Website" artboard of the Tally screens canvas.
 *
 * Store links are not published yet, so every download button scrolls to [SectionId.Download]
 * until [AndroidStoreUrl] and [IosStoreUrl] are filled in.
 */
public object TallySite {
    /** Set to the Play Store listing once it is live. */
    private val AndroidStoreUrl: String? = null

    /** Set to the App Store listing once it is live. */
    private val IosStoreUrl: String? = null

    private const val RepoUrl = "https://github.com/DuarteCPereira/Tally"

    /** The full page. */
    public val content: SiteContent by lazy { buildContent() }

    private fun buildContent(): SiteContent = SiteContent(
        brand = "Tally",
        nav = listOf(
            NavItem(Link(label = "How it works", target = SectionId.HowItWorks.target)),
            NavItem(Link(label = "Features", target = SectionId.Features.target)),
            NavItem(Link(label = "Statistics", target = SectionId.Statistics.target), badge = "Soon"),
        ),
        headerAction = Link(label = "Download", target = SectionId.Download.target),
        hero = hero(),
        principle = principle(),
        steps = steps(),
        features = features(),
        stats = StatsTeaser(
            eyebrow = Eyebrow(index = "05", text = "Statistics"),
            badge = "Coming soon",
            title = "Your progress, in numbers.",
            body = "Weekly volume, streaks and a trend for every exercise are on the way, " +
                "in the app and right here on the web.",
            previewNote = "preview · sample data",
        ),
        download = Download(
            title = "Start counting.",
            body = "Build your first routine in under a minute.",
            stores = stores(),
        ),
        footer = Footer(
            note = "© 2026 Tally",
            links = listOf(
                Link(label = "Privacy", target = LinkTarget.Url("$RepoUrl#why-tally")),
                Link(label = "Contact", target = LinkTarget.Url("$RepoUrl/issues")),
            ),
        ),
    )

    private fun hero() = Hero(
        chip = "Workout tracker · Android and iOS",
        title = "Every rep, counted.",
        body = "Tally is a simple workout tracker. Build a routine in minutes, follow it set by set, " +
            "and watch every session add up.",
        stores = stores(),
        tagline = "build → run → review → repeat",
        screen = AppScreen.Run,
        // Positions point at the round title, the rep counter and the main button of the run screen.
        callouts = listOf(
            Callout(text = "round 1 / 3", side = Callout.Side.Start, topFraction = 0.16f),
            Callout(text = "reps 12 / 12", side = Callout.Side.End, topFraction = 0.52f),
            Callout(text = "one tap per set", side = Callout.Side.Start, topFraction = 0.88f),
        ),
    )

    private fun principle() = Principle(
        eyebrow = Eyebrow(index = "00", text = "Principle"),
        title = "One screen. One action. Nothing in the way.",
        body = "Tally does less on purpose. Every screen has one clear next step, so you spend your time " +
            "training, not tapping.",
        facts = listOf(
            Fact(term = "primary actions per screen", value = "1"),
            Fact(term = "taps to log a set", value = "1"),
            Fact(term = "accent colours", value = "1", accent = true),
            Fact(term = "themes", value = "light / dark"),
        ),
    )

    private fun steps() = Steps(
        eyebrow = Eyebrow(index = "01–03", text = "How it works"),
        title = "Three steps, on repeat.",
        items = listOf(
            Step(
                eyebrow = Eyebrow(index = "01", text = "Build"),
                title = "Plan it once",
                body = "Pick exercises, group them into supersets or circuits, set reps and rest. " +
                    "Save it as a routine.",
                screen = AppScreen.Editor,
            ),
            Step(
                eyebrow = Eyebrow(index = "02", text = "Run"),
                title = "Follow along",
                body = "Tally walks you through every set. Log your reps with a tap and the rest timer " +
                    "starts on its own.",
                screen = AppScreen.Rest,
            ),
            Step(
                eyebrow = Eyebrow(index = "03", text = "Review"),
                title = "See it add up",
                body = "Every session lands in your history, set by set, so you know exactly what to " +
                    "beat next time.",
                screen = AppScreen.Summary,
            ),
        ),
    )

    private fun features() = Features(
        eyebrow = Eyebrow(index = "04", text = "Features"),
        title = "Everything you need. Nothing you don't.",
        items = listOf(
            Feature(
                index = "01",
                title = "Supersets and circuits",
                body = "Group exercises and Tally runs them in order, round after round.",
                icon = FeatureIcon.Sets,
            ),
            Feature(
                index = "02",
                title = "Exercise library",
                body = "Find an exercise in seconds and drop it straight into your routine.",
                icon = FeatureIcon.Search,
            ),
            Feature(
                index = "03",
                title = "Rest timer",
                body = "Starts on its own after every set, so you never count seconds.",
                icon = FeatureIcon.Timer,
            ),
            Feature(
                index = "04",
                title = "Sound and vibration cues",
                body = "Know when rest is over without looking at your phone.",
                icon = FeatureIcon.Sound,
            ),
            Feature(
                index = "05",
                title = "Full history",
                body = "Every set, rep and skip is saved, session by session.",
                icon = FeatureIcon.Trend,
            ),
            Feature(
                index = "06",
                title = "Light and dark",
                body = "Every screen is designed for both, morning session or late night.",
                icon = FeatureIcon.Theme,
            ),
        ),
    )

    private fun stores() = listOf(
        StoreLink(platform = "Android", target = AndroidStoreUrl.orDownload(), primary = true),
        StoreLink(platform = "iOS", target = IosStoreUrl.orDownload(), primary = false),
    )

    private fun String?.orDownload(): LinkTarget = this?.let(LinkTarget::Url) ?: SectionId.Download.target
}

/** A link to this section. */
public val SectionId.target: LinkTarget get() = LinkTarget.Section(this)
