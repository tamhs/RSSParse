package com.pet.rsspaser

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Created by tam.hs on 1/22/2024.
 */
@HiltAndroidApp
class RssParserApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}