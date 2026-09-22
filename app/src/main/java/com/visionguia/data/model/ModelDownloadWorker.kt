package com.visionguia.data.model

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.last
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class ModelDownloadWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val manager = ModelManager(applicationContext, OkHttpClient.Builder().readTimeout(15, TimeUnit.MINUTES).build())
        if (manager.isVerified(ModelCatalog.localLlm)) return Result.success()
        return when (manager.download(ModelCatalog.localLlm).last()) {
            is ModelDownloadState.Ready -> Result.success()
            else -> Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_NAME = "download-local-llm"
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresStorageNotLow(true)
                .build()
            val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>().setConstraints(constraints).build()
            WorkManager.getInstance(context).enqueueUniqueWork(UNIQUE_NAME, ExistingWorkPolicy.KEEP, request)
        }
    }
}
