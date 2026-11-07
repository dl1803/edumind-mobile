package com.edumind.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import com.edumind.app.repository.NoteSyncCoordinator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EduMindApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var noteSyncCoordinator: NoteSyncCoordinator

    override fun onCreate() {
        super.onCreate()
        noteSyncCoordinator.start()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}
