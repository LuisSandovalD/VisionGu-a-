package com.visionguia.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore("visionguia_settings")
data class UserSettings(val speechRate: Float = 1f, val vibration: Boolean = true, val detailLevel: Int = 1, val onboardingComplete: Boolean = false)

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val speechRate = floatPreferencesKey("speech_rate")
        val vibration = booleanPreferencesKey("vibration")
        val detail = intPreferencesKey("detail_level")
        val onboarding = booleanPreferencesKey("onboarding_complete")
    }
    val settings: Flow<UserSettings> = context.settingsDataStore.data.map {
        UserSettings(it[Keys.speechRate] ?: 1f, it[Keys.vibration] ?: true, it[Keys.detail] ?: 1, it[Keys.onboarding] ?: false)
    }
    suspend fun setSpeechRate(value: Float) = context.settingsDataStore.edit { it[Keys.speechRate] = value.coerceIn(.5f, 2f) }
    suspend fun setVibration(value: Boolean) = context.settingsDataStore.edit { it[Keys.vibration] = value }
    suspend fun completeOnboarding() = context.settingsDataStore.edit { it[Keys.onboarding] = true }
}
