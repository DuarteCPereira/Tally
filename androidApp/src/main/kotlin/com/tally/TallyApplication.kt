package com.tally

import android.app.Application
import com.tally.di.initKoin
import org.koin.android.ext.koin.androidContext

class TallyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@TallyApplication) }
    }
}
