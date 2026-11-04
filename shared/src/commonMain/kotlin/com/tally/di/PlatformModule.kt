package com.tally.di

import org.koin.core.module.Module

/**
 * The platform's implementations of the interfaces in `Platform.kt` and of the database `SqlDriver`,
 * which is the only part of [dataModule] that differs per platform.
 */
expect val platformModule: Module
