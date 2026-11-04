package com.tally.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatformTools

/**
 * Starts Koin with the app's modules, unless it is already running (the iOS view controller can be
 * created more than once). [declaration] configures the platform, e.g. `androidContext`.
 */
fun initKoin(declaration: KoinAppDeclaration = {}): KoinApplication? =
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        startKoin {
            declaration()
            modules(platformModule, AppModule().module())
        }
    } else {
        null
    }
