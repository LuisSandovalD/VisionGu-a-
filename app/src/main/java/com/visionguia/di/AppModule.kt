package com.visionguia.di

import android.content.Context
import androidx.room.Room
import com.visionguia.data.local.VisionGuiaDao
import com.visionguia.data.local.VisionGuiaDatabase
import com.visionguia.voice.AndroidSpeechSynthesisEngine
import com.visionguia.voice.SpeechRecognitionEngine
import com.visionguia.voice.SpeechSynthesisEngine
import com.visionguia.voice.VoskSpeechRecognitionEngine
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EngineModule {
    @Binds abstract fun speechRecognition(impl: VoskSpeechRecognitionEngine): SpeechRecognitionEngine
    @Binds abstract fun speechSynthesis(impl: AndroidSpeechSynthesisEngine): SpeechSynthesisEngine
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): VisionGuiaDatabase =
        Room.databaseBuilder(context, VisionGuiaDatabase::class.java, "visionguia.db")
            .fallbackToDestructiveMigration()
            .build()
    @Provides fun dao(database: VisionGuiaDatabase): VisionGuiaDao = database.dao()
    @Provides @Singleton fun httpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS).readTimeout(10, TimeUnit.MINUTES).build()
}
