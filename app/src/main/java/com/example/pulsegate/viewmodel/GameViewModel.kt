package com.example.pulsegate.viewmodel

import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulsegate.audio.SoundEffect
import com.example.pulsegate.audio.SoundManager
import com.example.pulsegate.data.PreferencesManager
import com.example.pulsegate.data.UserSettings
import com.example.pulsegate.game.GameEngine
import com.example.pulsegate.game.GamePhase
import com.example.pulsegate.game.GameState
import com.example.pulsegate.haptics.HapticType
import com.example.pulsegate.haptics.HapticsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class GameOverEvent(
    val score: Int,
    val isNewBest: Boolean,
    val perfectCount: Int,
    val gatesCount: Int
)

class GameViewModel(
    val soundManager: SoundManager,
    val hapticsManager: HapticsManager,
    val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _gameOverEvent = MutableSharedFlow<GameOverEvent>()
    val gameOverEvent: SharedFlow<GameOverEvent> = _gameOverEvent.asSharedFlow()

    val userSettings: StateFlow<UserSettings> = preferencesManager.userSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserSettings()
        )

    val gameEngine: GameEngine = GameEngine(
        soundManager = soundManager,
        hapticsManager = hapticsManager,
        onGameOverCallback = { score, isNewBest, perfectCount, gatesCount ->
            viewModelScope.launch {
                preferencesManager.saveBestScore(score)
                _gameOverEvent.emit(
                    GameOverEvent(
                        score = score,
                        isNewBest = isNewBest,
                        perfectCount = perfectCount,
                        gatesCount = gatesCount
                    )
                )
            }
        },
        onTutorialPassedCallback = {
            viewModelScope.launch {
                preferencesManager.setTutorialCompleted(true)
            }
        }
    )

    val gameState: StateFlow<GameState> = gameEngine.gameState

    private var gameLoopJob: Job? = null
    private var countdownJob: Job? = null

    init {
        // Sync settings to engine and managers
        viewModelScope.launch {
            userSettings.collect { settings ->
                soundManager.isSoundEnabled = settings.soundEnabled
                hapticsManager.isHapticsEnabled = settings.hapticsEnabled
                gameEngine.particlesEnabled = settings.particlesEnabled
                gameEngine.reducedEffects = settings.reducedEffects
            }
        }
    }

    fun startNewGame() {
        stopLoop()
        countdownJob?.cancel()

        viewModelScope.launch {
            val settings = preferencesManager.userSettingsFlow.first()
            val bestScore = settings.bestScore
            val tutorialDone = settings.tutorialCompleted

            gameEngine.startNewGame(bestScore = bestScore, tutorialCompleted = tutorialDone)
            runCountdown()
        }
    }

    private fun runCountdown() {
        countdownJob = viewModelScope.launch {
            // 3
            gameEngine.updateCountdown(3)
            delay(650)
            // 2
            gameEngine.updateCountdown(2)
            delay(650)
            // 1
            gameEngine.updateCountdown(1)
            delay(650)
            // GO
            gameEngine.updateCountdown(0)
            delay(400)
            // Start play
            gameEngine.updateCountdown(-1)
        }
    }

    fun updateFrame(dt: Float) {
        gameEngine.update(dt)
    }

    fun onTap() {
        gameEngine.onTapToDash()
    }

    fun pause() {
        gameEngine.pauseGame()
    }

    fun resume() {
        gameEngine.resumeGame()
    }

    fun restart() {
        startNewGame()
    }

    private fun stopLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopLoop()
        countdownJob?.cancel()
        soundManager.release()
    }
}
