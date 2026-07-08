package com.carlosarancibia.playfit

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PlayfitApp : Application(), SingletonImageLoader.Factory, Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context).build()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
