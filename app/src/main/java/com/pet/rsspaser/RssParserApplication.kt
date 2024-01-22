package com.pet.rsspaser

import android.app.Application
import com.pet.rsspaser.di.apiModule
import com.pet.rsspaser.di.repositoryModule
import com.pet.rsspaser.di.retrofitModule
import com.pet.rsspaser.di.viewModelModule
import org.koin.core.context.startKoin

/**
 * Created by tam.hs on 1/22/2024.
 */
class RssParserApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(listOf(viewModelModule, repositoryModule, apiModule, retrofitModule))
        }
    }
}