package com.example.pulsegate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.pulsegate.audio.SoundManager
import com.example.pulsegate.data.PreferencesManager
import com.example.pulsegate.data.ThemeMode
import com.example.pulsegate.haptics.HapticsManager
import com.example.pulsegate.ui.navigation.PulseGateNavigation
import com.example.pulsegate.ui.theme.PulseGateTheme
import com.example.pulsegate.viewmodel.GameViewModel
import com.example.pulsegate.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var soundManager: SoundManager
    private lateinit var hapticsManager: HapticsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferencesManager = PreferencesManager(applicationContext)
        soundManager = SoundManager()
        hapticsManager = HapticsManager(applicationContext)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return SettingsViewModel(preferencesManager) as T
                    }
                }
            )

            val gameViewModel: GameViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return GameViewModel(
                            soundManager = soundManager,
                            hapticsManager = hapticsManager,
                            preferencesManager = preferencesManager
                        ) as T
                    }
                }
            )

            val userSettings by settingsViewModel.userSettings.collectAsState()
            val themeMode = userSettings.themeMode

            val systemInDark = isSystemInDarkTheme()
            val isLightMode = when (themeMode) {
                ThemeMode.LIGHT -> true
                ThemeMode.DARK -> false
                ThemeMode.SYSTEM -> !systemInDark
            }

            PulseGateTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    PulseGateNavigation(
                        navController = navController,
                        gameViewModel = gameViewModel,
                        settingsViewModel = settingsViewModel,
                        isLightMode = isLightMode
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
    }
}