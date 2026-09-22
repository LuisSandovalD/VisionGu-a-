package com.visionguia

import android.app.Application
import com.visionguia.data.model.ModelDownloadWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VisionGuiaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ModelDownloadWorker.enqueue(this)
    }
}
