package com.example.pulsegate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulsegate.data.PreferencesManager
import com.example.pulsegate.data.ThemeMode
import com.example.pulsegate.data.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = preferencesManager.userSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSoundEnabled(enabled)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setHapticsEnabled(enabled)
        }
    }

    fun setParticlesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setParticlesEnabled(enabled)
        }
    }

    fun setReducedEffects(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setReducedEffects(enabled)
        }
    }

    fun resetBestScore() {
        viewModelScope.launch {
            preferencesManager.resetBestScore()
        }
    }
}
