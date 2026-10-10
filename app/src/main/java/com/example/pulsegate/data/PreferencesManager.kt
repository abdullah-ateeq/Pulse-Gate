package com.example.pulsegate.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pulse_gate_settings")

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val PARTICLES_ENABLED = booleanPreferencesKey("particles_enabled")
        val REDUCED_EFFECTS = booleanPreferencesKey("reduced_effects")
        val TUTORIAL_COMPLETED = booleanPreferencesKey("tutorial_completed")
        val BEST_SCORE = intPreferencesKey("best_score")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeModeName = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.DARK.name
            val themeMode = try {
                ThemeMode.valueOf(themeModeName)
            } catch (e: IllegalArgumentException) {
                ThemeMode.DARK
            }

            UserSettings(
                themeMode = themeMode,
                soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
                hapticsEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true,
                particlesEnabled = preferences[PreferencesKeys.PARTICLES_ENABLED] ?: true,
                reducedEffects = preferences[PreferencesKeys.REDUCED_EFFECTS] ?: false,
                tutorialCompleted = preferences[PreferencesKeys.TUTORIAL_COMPLETED] ?: false,
                bestScore = preferences[PreferencesKeys.BEST_SCORE] ?: 0
            )
        }

    val bestScoreFlow: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.BEST_SCORE] ?: 0
        }

    val tutorialCompletedFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.TUTORIAL_COMPLETED] ?: false
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setParticlesEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PARTICLES_ENABLED] = enabled
        }
    }

    suspend fun setReducedEffects(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REDUCED_EFFECTS] = enabled
        }
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TUTORIAL_COMPLETED] = completed
        }
    }

    suspend fun saveBestScore(score: Int) {
        context.dataStore.edit { preferences ->
            val currentBest = preferences[PreferencesKeys.BEST_SCORE] ?: 0
            if (score > currentBest) {
                preferences[PreferencesKeys.BEST_SCORE] = score
            }
        }
    }

    suspend fun resetBestScore() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BEST_SCORE] = 0
        }
    }
}
